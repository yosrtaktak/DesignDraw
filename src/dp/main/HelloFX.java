package dp.main;

import dp.DS.command.AddShapeCommand;
import dp.DS.command.CommandManager;
import dp.DS.command.EraseShapeCommand;
import dp.DS.command.ICommand;
import dp.DS.observer.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import dp.DS.singleton.Logger;
import dp.DS.strategy.LogConsole;
import dp.DS.strategy.LogDB;
import dp.DS.strategy.LogFile;

public class HelloFX extends Application {

    private double startX, startY;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        Logger logger = Logger.getInstance();
        logger.log("Application demarree");

        // --- Command Manager (deux piles : undo et redo) ---
        CommandManager cmdManager = new CommandManager();

        BorderPane root = new BorderPane();
        DrawingCanvas drawingCanvas = new DrawingCanvas(800, 400);

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
                }
        );
        palette.setSelectedLogger("LogConsole");

        // Observer : palette → canvas
        palette.addObserver(drawingCanvas);

        // --- Mouse events sur le canvas ---
        drawingCanvas.getCanvas().setOnMousePressed(e -> {
            startX = e.getX();
            startY = e.getY();
        });

        drawingCanvas.getCanvas().setOnMouseReleased(e -> {
            double endX = e.getX();
            double endY = e.getY();

            if (palette.isEraserMode()) {
                // --- GOMME : EraseShapeCommand ---
                IShape lastShape = drawingCanvas.getLastShape();
                if (lastShape != null) {
                    ICommand cmd = new EraseShapeCommand(drawingCanvas, lastShape);
                    cmdManager.executeCommand(cmd);
                    logger.log("Gomme: forme effacee - " + lastShape.toString());
                }
            } else {
                // --- DESSIN : AddShapeCommand ---
                IShape shape = palette.createShape(startX, startY, endX, endY);
                if (shape != null) {
                    ICommand cmd = new AddShapeCommand(drawingCanvas, shape);
                    cmdManager.executeCommand(cmd);
                    logger.log("Shape drawn: " + shape.toString());
                }
            }
        });

        root.setTop(palette.getView());
        root.setCenter(drawingCanvas.getCanvas());

        Scene scene = new Scene(root, 800, 500);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Dessiner des Formes - JavaFX");
        primaryStage.show();

        logger.log("Interface graphique initialisee");
    }
}
