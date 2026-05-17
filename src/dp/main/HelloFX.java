package dp.main;

import dp.DS.command.AddShapeCommand;
import dp.DS.command.CommandManager;
import dp.DS.command.EraseShapeCommand;
import dp.DS.command.ICommand;
import dp.DS.command.ResizeShapeCommand;
import dp.DS.graph.BFSStrategy;
import dp.DS.graph.BellmanFordStrategy;
import dp.DS.graph.DijkstraStrategy;
import dp.DS.graph.Graph;
import dp.DS.graph.GraphBuilder;
import dp.DS.graph.PathCalculator;
import dp.DS.graph.PathResult;
import dp.DS.observer.*;
import dp.DS.persistence.DrawingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import dp.DS.singleton.Logger;
import dp.DS.strategy.LogConsole;
import dp.DS.strategy.LogDB;
import dp.DS.strategy.LogFile;

public class HelloFX extends Application {

    private double startX, startY;
    private IShape resizeTarget;
    /** Premier nœud sélectionné en mode "plus court chemin" (centre [x,y]) ou null. */
    private double[] pathFirst;
    /** Nom du dessin courant (dernier enregistré / ouvert) ou null. */
    private String currentDrawingName;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        Logger logger = Logger.getInstance();
        logger.log("Application demarree");

        // --- Command Manager (deux piles : undo et redo) ---
        CommandManager cmdManager = new CommandManager();

        // --- Strategy : calcul du plus court chemin (Dijkstra par defaut) ---
        final PathCalculator pathCalculator = new PathCalculator();
        final GraphBuilder graphBuilder = new GraphBuilder();

        // --- DAO : enregistrement / ouverture du dessin (PostgreSQL) ---
        final DrawingRepository repository = new DrawingRepository();

        BorderPane root = new BorderPane();
        DrawingCanvas drawingCanvas = new DrawingCanvas(800, 400);

        // --- Barre de statut : affiche le resultat des actions (ex: calcul) ---
        final Label statusBar = new Label("Pret");
        statusBar.setMaxWidth(Double.MAX_VALUE);
        statusBar.setStyle(
                "-fx-background-color: #2b2b2b;" +
                "-fx-text-fill: #f0f0f0;" +
                "-fx-padding: 6 12 6 12;" +
                "-fx-font-size: 13px;"
        );

        ToolPalette palette = new ToolPalette(
                // onRectangle
                () -> logger.log("Shape selected: RECTANGLE"),
                // onCircle
                () -> logger.log("Shape selected: CIRCLE"),
                // onLine
                () -> logger.log("Shape selected: LINE"),
                // onUndo — utilise la pile undo du CommandManager
                () -> {
                    cmdManager.undo();
                    logger.log("Undo - shapes restantes: " + drawingCanvas.getShapeCount());
                },
                // onRedo — utilise la pile redo du CommandManager
                () -> {
                    cmdManager.redo();
                    logger.log("Redo - shapes restantes: " + drawingCanvas.getShapeCount());
                },
                // onEraser (gomme)
                () -> logger.log("Mode Gomme active"),
                // onResize
                () -> logger.log("Mode Redimensionner active"),
                // onLoggerChange
                selected -> {
                    switch (selected) {
                        case "LogConsole":
                            logger.setStrategy(new LogConsole());
                            break;
                        case "LogFile":
                            logger.setStrategy(new LogFile());
                            break;
                        case "LogDB":
                            logger.setStrategy(new LogDB());
                            break;
                    }
                    logger.log("Logger strategy changed to: " + selected);
                },
                // onPath — entre en mode "plus court chemin"
                () -> {
                    pathFirst = null;
                    drawingCanvas.clearHighlight();
                    drawingCanvas.redraw();
                    statusBar.setText("Mode Plus court chemin : cliquez 2 formes (les lignes = arêtes, sinon distance directe)");
                    logger.log("Mode Plus court chemin active");
                },
                // onAlgoChange — Strategy : choisit l'algorithme du plus court chemin
                algo -> {
                    switch (algo) {
                        case "Dijkstra":
                            pathCalculator.setStrategy(new DijkstraStrategy());
                            break;
                        case "Bellman-Ford":
                            pathCalculator.setStrategy(new BellmanFordStrategy());
                            break;
                        case "BFS":
                            pathCalculator.setStrategy(new BFSStrategy());
                            break;
                    }
                    statusBar.setText("Algorithme sélectionné : " + algo);
                    logger.log("Algorithme plus court chemin: " + algo);
                },
                // onSave — enregistre le dessin nommé dans PostgreSQL (DAO)
                () -> {
                    TextInputDialog dlg = new TextInputDialog(
                            currentDrawingName == null ? "MonDessin" : currentDrawingName);
                    dlg.initOwner(primaryStage);
                    dlg.setTitle("Enregistrer le dessin");
                    dlg.setHeaderText("Nom du dessin (un nom existant sera remplacé)");
                    dlg.setContentText("Nom :");
                    Optional<String> res = dlg.showAndWait();
                    if (!res.isPresent()) {
                        statusBar.setText("Enregistrement annulé");
                        return;
                    }
                    String name = res.get().trim();
                    if (name.isEmpty()) {
                        statusBar.setText("Nom vide — enregistrement annulé");
                        return;
                    }
                    int n = repository.save(name, drawingCanvas.getShapes());
                    if (n >= 0) {
                        currentDrawingName = name;
                        statusBar.setText("Dessin « " + name + " » enregistré — "
                                + n + " forme(s)");
                        logger.log("Dessin enregistre en base: " + name
                                + " (" + n + " formes)");
                    } else {
                        statusBar.setText("Erreur : enregistrement impossible (base indisponible)");
                        logger.log("Erreur enregistrement dessin (base indisponible)");
                    }
                },
                // onOpen — choisit puis recharge un dessin enregistré (DAO)
                () -> {
                    List<String> names = repository.listDrawings();
                    if (names == null) {
                        statusBar.setText("Erreur : ouverture impossible (base indisponible)");
                        logger.log("Erreur ouverture dessin (base indisponible)");
                        return;
                    }
                    if (names.isEmpty()) {
                        Alert info = new Alert(Alert.AlertType.INFORMATION,
                                "Aucun dessin n'est encore enregistré dans la base.");
                        info.initOwner(primaryStage);
                        info.setHeaderText("Ouvrir un dessin");
                        info.showAndWait();
                        statusBar.setText("Aucun dessin enregistré");
                        return;
                    }
                    ChoiceDialog<String> dlg = new ChoiceDialog<>(
                            names.contains(currentDrawingName)
                                    ? currentDrawingName : names.get(0),
                            names);
                    dlg.initOwner(primaryStage);
                    dlg.setTitle("Ouvrir un dessin");
                    dlg.setHeaderText("Choisissez un dessin enregistré");
                    dlg.setContentText("Dessin :");
                    Optional<String> sel = dlg.showAndWait();
                    if (!sel.isPresent()) {
                        statusBar.setText("Ouverture annulée");
                        return;
                    }
                    String name = sel.get();
                    List<IShape> loaded = repository.load(name);
                    if (loaded == null) {
                        statusBar.setText("Erreur : ouverture impossible (base indisponible)");
                        logger.log("Erreur ouverture dessin (base indisponible)");
                        return;
                    }
                    drawingCanvas.clearHighlight();
                    drawingCanvas.clearShapes();
                    for (IShape s : loaded) {
                        drawingCanvas.addShape(s);
                    }
                    pathFirst = null;
                    currentDrawingName = name;
                    statusBar.setText("Dessin « " + name + " » ouvert — "
                            + loaded.size() + " forme(s)");
                    logger.log("Dessin ouvert depuis la base: " + name
                            + " (" + loaded.size() + " formes)");
                }
        );
        palette.setSelectedLogger("LogConsole");

        // Observer : palette → canvas
        palette.addObserver(drawingCanvas);

        // --- Mouse events sur le canvas ---
        drawingCanvas.getCanvas().setOnMousePressed(e -> {
            startX = e.getX();
            startY = e.getY();
            if (palette.isResizeMode()) {
                resizeTarget = drawingCanvas.findShapeAt(startX, startY);
                if (resizeTarget == null) {
                    resizeTarget = drawingCanvas.getLastShape();
                }
            }
        });

        drawingCanvas.getCanvas().setOnMouseReleased(e -> {
            double endX = e.getX();
            double endY = e.getY();

            // --- ETUDE DE CAS : plus court chemin (Strategy) ---
            if (palette.isPathMode()) {
                IShape picked = drawingCanvas.findShapeAt(endX, endY);
                if (picked == null || "LINE".equals(picked.shapeKind())) {
                    statusBar.setText("Plus court chemin : cliquez sur une forme (nœud), pas une ligne");
                    logger.log("Plus court chemin: cliquez sur une forme noeud (pas une ligne)");
                    return;
                }
                if (pathFirst == null) {
                    pathFirst = new double[]{ picked.getCenterX(), picked.getCenterY() };
                    drawingCanvas.clearHighlight();
                    drawingCanvas.redraw();
                    statusBar.setText("Nœud source sélectionné — cliquez le nœud cible");
                    logger.log("Plus court chemin: noeud source selectionne");
                    return;
                }
                Graph g = graphBuilder.build(drawingCanvas.getShapes());
                int src = g.nearestNode(pathFirst[0], pathFirst[1], 1e9);
                int tgt = g.nearestNode(picked.getCenterX(), picked.getCenterY(), 1e9);
                if (src == -1 || tgt == -1) {
                    statusBar.setText("Cliquez bien sur une forme (nœud)");
                    pathFirst = null;
                    return;
                }
                if (src == tgt) {
                    statusBar.setText("Sélectionnez deux formes différentes");
                    pathFirst = null;
                    return;
                }
                PathResult res = pathCalculator.findPath(g, src, tgt);
                if (res.isFound() && res.getNodes().size() >= 2) {
                    List<double[]> pts = new ArrayList<>();
                    for (int idx : res.getNodes()) pts.add(g.nodePosition(idx));
                    drawingCanvas.setHighlightPath(pts);
                    String algoName = pathCalculator.getStrategy().getClass().getSimpleName()
                            .replace("Strategy", "");
                    String msg = algoName + " : chemin " + res.getNodes()
                            + "  —  distance = " + String.format("%.1f", res.getDistance());
                    statusBar.setText(msg);
                    logger.log("Plus court chemin (" + algoName + ") = " + res.getNodes()
                            + " | distance = " + String.format("%.1f", res.getDistance()));
                } else {
                    drawingCanvas.clearHighlight();
                    drawingCanvas.redraw();
                    statusBar.setText("Aucun chemin : nœuds dans des parties non reliées (reliez-les par une ligne)");
                    logger.log("Plus court chemin: aucun chemin (composants non relies)");
                }
                pathFirst = null;
                return;
            }

            // Toute autre action efface le surlignage du chemin precedent.
            drawingCanvas.clearHighlight();

            if (palette.isResizeMode()) {
                if (resizeTarget != null) {
                    int size = palette.getSelectedSize();
                    ICommand cmd = new ResizeShapeCommand(drawingCanvas, resizeTarget, size);
                    cmdManager.executeCommand(cmd);
                    statusBar.setText("Forme redimensionnée à la taille " + size);
                    logger.log("Redimensionnement -> taille " + size + " : " + resizeTarget.toString());
                }
                resizeTarget = null;
            } else if (palette.isEraserMode()) {
                // --- GOMME : efface la forme selectionnee (sous le curseur) ---
                IShape target = drawingCanvas.findShapeAt(endX, endY);
                if (target == null) target = drawingCanvas.findShapeAt(startX, startY);
                if (target != null) {
                    ICommand cmd = new EraseShapeCommand(drawingCanvas, target);
                    cmdManager.executeCommand(cmd);
                    statusBar.setText("Forme effacée — " + target.shapeKind());
                    logger.log("Gomme: forme effacee - " + target.toString());
                }
            } else {
                // --- DESSIN : AddShapeCommand ---
                IShape shape = palette.createShape(startX, startY, endX, endY);
                if (shape != null) {
                    ICommand cmd = new AddShapeCommand(drawingCanvas, shape);
                    cmdManager.executeCommand(cmd);
                    statusBar.setText("Forme dessinée — " + shape.shapeKind());
                    logger.log("Shape drawn: " + shape.toString());
                }
            }
        });

        root.setTop(palette.getView());
        root.setCenter(drawingCanvas.getView());
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 1200, 750);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Dessiner des Formes - JavaFX");
        primaryStage.show();

        logger.log("Interface graphique initialisee");
    }
}
