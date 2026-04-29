package dp.DS.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import dp.DS.Factory.CircleFactory;
import dp.DS.Factory.LineFactory;
import dp.DS.Factory.RectangleFactory;
import dp.DS.Factory.ShapeFactory;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

/**
 * Palette d'outils (boutons formes, undo, choix logger).
 */
public class ToolPalette extends Pane implements ShapeFactory, IObservable{

    private final HBox root;
    private final ComboBox<String> comboLogger;
    private final List<IObserver> observers = new ArrayList<>();
    private ShapeFactory sf;

    public ToolPalette(
            Runnable onRectangle,
            Runnable onCircle,
            Runnable onLine,
            Runnable onUndo,
            Consumer<String> onLoggerChange) {

        Button btnRectangle = new Button("Rectangle");
        Button btnCircle = new Button("Circle");
        Button btnLine = new Button("Line");
        Button btnUndo = new Button("Undo");

        Label lblLogger = new Label("Logger:");
        comboLogger = new ComboBox<>(
                FXCollections.observableArrayList("LogConsole", "LogFile", "LogDB")
        );
        comboLogger.setValue("LogConsole");

        btnRectangle.setOnAction(e -> {
            onRectangle.run();
            sf = new RectangleFactory();
            notifyObservers();
        });
        btnCircle.setOnAction(e -> {
            onCircle.run();
            sf = new CircleFactory();
            notifyObservers();
        });
        btnLine.setOnAction(e -> {
            onLine.run();
            sf = new LineFactory();
            notifyObservers();
        });
        btnUndo.setOnAction(e -> onUndo.run());
        comboLogger.setOnAction(e -> onLoggerChange.accept(comboLogger.getValue()));

        root = new HBox(10);
        root.setStyle(
                "-fx-padding: 10;" +
                        "-fx-background-color: #f4f4f4;" +
                        "-fx-border-color: #cccccc;" +
                        "-fx-border-width: 0 0 1 0;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);"
        );
        root.getChildren().addAll(btnRectangle, btnCircle, btnLine, btnUndo, lblLogger, comboLogger);
    }

    public HBox getView() {
        return root;
    }

    public void setSelectedLogger(String loggerName) {
        comboLogger.setValue(loggerName);
    }

    @Override
    public void addObserver(IObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(IObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (IObserver observer : observers) {
            observer.update();
        }
    }

    @Override
    public IShape createShape(double startX, double startY, double endX, double endY) {
        if (sf != null) {
            return sf.createShape(startX, startY, endX, endY);
        }
        return null;
    }
}
