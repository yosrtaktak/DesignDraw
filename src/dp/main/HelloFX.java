package dp.main;

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

    // Point de départ du dessin (mousePressed)
    private double startX, startY;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        Logger logger = Logger.getInstance();
        logger.log("Application demarree");

        BorderPane root = new BorderPane();
        DrawingCanvas drawingCanvas = new DrawingCanvas(800, 200);

        ToolPalette palette = new ToolPalette(
                () -> logger.log("Shape selected: RECTANGLE"),
                () -> logger.log("Shape selected: CIRCLE"),
                () -> logger.log("Shape selected: LINE"),
                () -> {
                    drawingCanvas.removeLastShape();
                    logger.log("Undo action triggered - shapes restantes: " + drawingCanvas.getShapeCount());
                },
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
                        default:
                            break;
                    }
                    logger.log("Logger strategy changed to: " + selected);
                }
        );
        palette.setSelectedLogger("LogConsole");

        // Lien de l'Observer selon l'architecture
        palette.addObserver(drawingCanvas);

        drawingCanvas.getCanvas().setOnMousePressed(e -> {
            startX = e.getX();
            startY = e.getY();
        });

        drawingCanvas.getCanvas().setOnMouseReleased(e -> {
            double endX = e.getX();
            double endY = e.getY();

            IShape shape = palette.createShape(startX, startY, endX, endY);

            if (shape != null) {
                drawingCanvas.addShape(shape);
                logger.log("Shape drawn: " + shape.toString());
            }
        });

        root.setTop(palette.getView());
        root.setBottom(drawingCanvas.getCanvas());

        Scene scene = new Scene(root, 700, 450);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Dessiner des formes - Strategy + Singleton + Observer");
        primaryStage.show();

        logger.log("Interface graphique initialisee");
    }
}
