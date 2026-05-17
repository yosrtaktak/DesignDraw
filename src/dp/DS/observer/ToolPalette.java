package dp.DS.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import dp.DS.Factory.RectangleFactory;
import dp.DS.Factory.RectangleFactory3D;
import dp.DS.Factory.CircleFactory;
import dp.DS.Factory.CircleFactory3D;
import dp.DS.Factory.LineFactory;
import dp.DS.Factory.LineFactory3D;
import dp.DS.Factory.ShapeFactory;
import dp.DS.decorator.BorderColorDecorator;
import dp.DS.decorator.FillColorDecorator;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleGroup;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Palette d'outils — conforme à la conception :
 * - Factory Method : ShapeFactory sf
 * - Observer : IObservable
 * - Command : Undo / Redo / Gomme / Redimensionner via callbacks
 * - Decorator : FillColor / BorderColor appliques dans createShape
 * - Strategy : choix de l'algorithme de plus court chemin (Dijkstra/...)
 */
public class ToolPalette extends Pane implements IObservable {

    private final VBox root;
    private final ComboBox<String> comboLogger;
    private final List<IObserver> observers = new ArrayList<>();

    private final ColorPicker fillColorPicker;
    private final CheckBox cbBorder;
    private final ColorPicker borderColorPicker;
    private final CheckBox cbFill;
    private final ComboBox<Integer> comboResize;
    private final CheckBox cbFixedSize;

    private ShapeFactory sf;
    private String selectedShapeType;
    private boolean is3D = false;
    private boolean eraserMode = false;
    private boolean resizeMode = false;
    private boolean pathMode = false;

    public ToolPalette(
            Runnable onRectangle,
            Runnable onCircle,
            Runnable onLine,
            Runnable onUndo,
            Runnable onRedo,
            Runnable onEraser,
            Runnable onResize,
            Consumer<String> onLoggerChange,
            Runnable onPath,
            Consumer<String> onAlgoChange,
            Runnable onSave,
            Runnable onOpen) {

        // --- Boutons forme ---
        Button btnRectangle = new Button("Rectangle");
        Button btnCircle    = new Button("Cercle");
        Button btnLine      = new Button("Ligne");

        // --- Boutons commande ---
        Button btnUndo   = new Button("Undo");
        Button btnRedo   = new Button("Redo");
        Button btnEraser = new Button("Gomme");
        Button btnSave   = new Button("Enregistrer");
        Button btnOpen   = new Button("Ouvrir");

        // --- Etude de cas : plus court chemin dans un graphe ---
        Button btnPath = new Button("Plus court chemin");
        Label lblAlgo = new Label("Algo:");
        ComboBox<String> comboAlgo = new ComboBox<>(
                FXCollections.observableArrayList("Dijkstra", "Bellman-Ford", "BFS")
        );
        comboAlgo.setValue("Dijkstra");

        // --- Selecteur de taille (remplace le bouton Redimensionner) ---
        Label lblResize = new Label("Taille:");
        comboResize = new ComboBox<>(
                FXCollections.observableArrayList(5, 10, 15, 20, 25, 30, 40, 50, 75, 100, 150, 200)
        );
        comboResize.setValue(50);
        // Quand cochee : la forme dessinee prend la taille choisie au lieu
        // de la taille definie par le glisser de la souris.
        cbFixedSize = new CheckBox("Taille fixe");

        // --- Radio 2D / 3D ---
        ToggleGroup dimensionGroup = new ToggleGroup();
        RadioButton rb2D = new RadioButton("2D");
        RadioButton rb3D = new RadioButton("3D");
        rb2D.setToggleGroup(dimensionGroup);
        rb3D.setToggleGroup(dimensionGroup);
        rb2D.setSelected(true);

        // --- Controles Decorator : Couleur / Bordure ---
        Label lblFill = new Label("Couleur:");
        fillColorPicker = new ColorPicker(Color.LIGHTBLUE);
        cbFill = new CheckBox("Remplir");
        cbFill.setSelected(true);

        Label lblBorder = new Label("Bordure:");
        cbBorder = new CheckBox();
        cbBorder.setSelected(true);
        borderColorPicker = new ColorPicker(Color.BLACK);

        // --- Logger combo ---
        Label lblLogger = new Label("Logger:");
        comboLogger = new ComboBox<>(
                FXCollections.observableArrayList("LogConsole", "LogFile", "LogDB")
        );
        comboLogger.setValue("LogConsole");

        // --- Actions boutons forme ---
        btnRectangle.setOnAction(e -> {
            eraserMode = false;
            resizeMode = false;
            pathMode = false;
            onRectangle.run();
            selectedShapeType = "RECTANGLE";
            sf = is3D ? new RectangleFactory3D() : new RectangleFactory();
            notifyObservers();
        });
        btnCircle.setOnAction(e -> {
            eraserMode = false;
            resizeMode = false;
            pathMode = false;
            onCircle.run();
            selectedShapeType = "CIRCLE";
            sf = is3D ? new CircleFactory3D() : new CircleFactory();
            notifyObservers();
        });
        btnLine.setOnAction(e -> {
            eraserMode = false;
            resizeMode = false;
            pathMode = false;
            onLine.run();
            selectedShapeType = "LINE";
            sf = is3D ? new LineFactory3D() : new LineFactory();
            notifyObservers();
        });

        // --- Actions commande (Command pattern) ---
        btnUndo.setOnAction(e -> onUndo.run());
        btnRedo.setOnAction(e -> onRedo.run());
        btnSave.setOnAction(e -> onSave.run());
        btnOpen.setOnAction(e -> onOpen.run());
        btnEraser.setOnAction(e -> {
            eraserMode = true;
            resizeMode = false;
            pathMode = false;
            onEraser.run();
        });
        comboResize.setOnAction(e -> {
            resizeMode = true;
            eraserMode = false;
            pathMode = false;
            onResize.run();
        });
        btnPath.setOnAction(e -> {
            pathMode = true;
            eraserMode = false;
            resizeMode = false;
            onPath.run();
        });
        comboAlgo.setOnAction(e -> onAlgoChange.accept(comboAlgo.getValue()));

        // --- Actions radio 2D / 3D ---
        rb2D.setOnAction(e -> { is3D = false; updateFactory(); });
        rb3D.setOnAction(e -> { is3D = true;  updateFactory(); });

        comboLogger.setOnAction(e -> onLoggerChange.accept(comboLogger.getValue()));

        // --- Style des boutons (visuel uniquement, aucun changement de logique) ---
        styleButton(btnRectangle, false);
        styleButton(btnCircle, false);
        styleButton(btnLine, false);
        styleButton(btnUndo, false);
        styleButton(btnRedo, false);
        styleButton(btnEraser, false);
        styleButton(btnPath, true);
        styleButton(btnSave, true);
        styleButton(btnOpen, true);

        // --- Layout : sections étiquetées, barre compacte (peu d'espace) ---
        FlowPane bar = new FlowPane(10, 4);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getChildren().addAll(
                section("Formes", group(btnRectangle, btnCircle, btnLine)), sep(),
                section("Dimension", group(rb2D, rb3D,
                        new Separator(Orientation.VERTICAL),
                        lblResize, comboResize, cbFixedSize)), sep(),
                section("Apparence", group(lblFill, fillColorPicker, cbFill,
                        new Separator(Orientation.VERTICAL),
                        lblBorder, cbBorder, borderColorPicker)), sep(),
                section("Édition", group(btnUndo, btnRedo, btnEraser)), sep(),
                section("Graphe", group(btnPath, lblAlgo, comboAlgo)), sep(),
                section("Fichier", group(btnSave, btnOpen)), sep(),
                section("Journal", group(lblLogger, comboLogger))
        );

        root = new VBox(bar);
        root.setPadding(new Insets(5, 8, 5, 8));
        root.setStyle(
                "-fx-background-color: #EEF1F4;" +
                "-fx-border-color: #D7DBE0;" +
                "-fx-border-width: 0 0 1 0;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 6, 0, 0, 2);"
        );
    }

    // --- Helpers de présentation (purement visuels) ---

    private static final String BTN_BASE =
            "-fx-background-radius: 6; -fx-border-radius: 6; -fx-cursor: hand;" +
            "-fx-padding: 3 9 3 9; -fx-font-size: 11px;";

    private static String btnStyle(boolean primary, boolean hover) {
        if (primary) {
            return BTN_BASE + "-fx-text-fill: white; -fx-font-weight: bold;"
                    + "-fx-background-color: " + (hover ? "#1D4ED8" : "#2563EB") + ";";
        }
        return BTN_BASE + "-fx-text-fill: #1F2937; -fx-border-width: 1;"
                + "-fx-background-color: " + (hover ? "#F3F4F6" : "white") + ";"
                + "-fx-border-color: " + (hover ? "#9CA3AF" : "#D0D5DD") + ";";
    }

    /** Applique un style cohérent + survol. N'altère pas l'action du bouton. */
    private static void styleButton(Button b, boolean primary) {
        b.setStyle(btnStyle(primary, false));
        b.setOnMouseEntered(e -> b.setStyle(btnStyle(primary, true)));
        b.setOnMouseExited(e -> b.setStyle(btnStyle(primary, false)));
    }

    /** Regroupe des contrôles horizontalement, alignés et espacés. */
    private static HBox group(Node... items) {
        HBox h = new HBox(6, items);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    /** Petit intitulé de section suivi de ses contrôles, sur une seule ligne. */
    private static Node section(String title, Node content) {
        Label cap = new Label(title.toUpperCase());
        cap.setStyle("-fx-font-size: 9px; -fx-font-weight: bold;"
                + "-fx-text-fill: #98A0AC;");
        HBox h = new HBox(6, cap, content);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    /** Séparateur vertical fin entre deux sections. */
    private static Separator sep() {
        return new Separator(Orientation.VERTICAL);
    }

    private void updateFactory() {
        if (selectedShapeType == null) return;
        switch (selectedShapeType) {
            case "RECTANGLE":
                sf = is3D ? new RectangleFactory3D() : new RectangleFactory();
                break;
            case "CIRCLE":
                sf = is3D ? new CircleFactory3D() : new CircleFactory();
                break;
            case "LINE":
                sf = is3D ? new LineFactory3D() : new LineFactory();
                break;
        }
    }

    /**
     * Cree la forme via la factory puis empile les decorators selon l'etat de la palette.
     */
    public IShape createShape(double startX, double startY, double endX, double endY) {
        if (sf == null) return null;

        double ex = endX, ey = endY;
        if (cbFixedSize.isSelected()) {
            double size = getSelectedSize();
            if ("LINE".equals(selectedShapeType)) {
                double dx = endX - startX;
                double dy = endY - startY;
                double len = Math.sqrt(dx * dx + dy * dy);
                if (len == 0) { ex = startX + size; ey = startY; }
                else { ex = startX + dx / len * size; ey = startY + dy / len * size; }
            } else {
                ex = startX + size;
                ey = startY + size;
            }
        }

        IShape shape = sf.createShape(startX, startY, ex, ey);
        if (shape == null) return null;

        boolean isLine = "LINE".equals(selectedShapeType);

        if (isLine) {
            // Une ligne n'a pas de remplissage : sa couleur = le sélecteur "Couleur".
            shape = new BorderColorDecorator(shape, fillColorPicker.getValue());
        } else {
            if (cbFill.isSelected()) {
                shape = new FillColorDecorator(shape, fillColorPicker.getValue());
            }
            // La bordure n'est appliquée que si la case est cochée (plus forcée).
            if (cbBorder.isSelected()) {
                shape = new BorderColorDecorator(shape, borderColorPicker.getValue());
            }
        }
        return shape;
    }

    public boolean isEraserMode() { return eraserMode; }

    public boolean isResizeMode() { return resizeMode; }

    public boolean isPathMode() { return pathMode; }

    /** Taille absolue choisie dans le selecteur (defaut 50). */
    public int getSelectedSize() {
        Integer v = comboResize.getValue();
        return v == null ? 50 : v;
    }

    public VBox getView() { return root; }

    public void setSelectedLogger(String loggerName) {
        comboLogger.setValue(loggerName);
    }

    @Override
    public void addObserver(IObserver observer) { observers.add(observer); }
    @Override
    public void removeObserver(IObserver observer) { observers.remove(observer); }
    @Override
    public void notifyObservers() {
        for (IObserver observer : observers) { observer.update(); }
    }
}
