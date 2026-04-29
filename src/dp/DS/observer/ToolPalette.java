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
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

/**
 * Palette d'outils — conforme à la conception :
 * - Factory Method : ShapeFactory sf
 * - Observer : IObservable
 * - Command : Undo / Redo / Gomme via callbacks
 */
public class ToolPalette extends Pane implements IObservable {

    private final HBox root;
    private final ComboBox<String> comboLogger;
    private final List<IObserver> observers = new ArrayList<>();

    private ShapeFactory sf;
    private String selectedShapeType;
    private boolean is3D = false;
    private boolean eraserMode = false;

    public ToolPalette(
            Runnable onRectangle,
            Runnable onCircle,
            Runnable onLine,
            Runnable onUndo,
            Runnable onRedo,
            Runnable onEraser,
            Consumer<String> onLoggerChange) {

        // --- Boutons forme ---
        Button btnRectangle = new Button("Rectangle");
        Button btnCircle    = new Button("Cercle");
        Button btnLine      = new Button("Ligne");

        // --- Boutons commande ---
        Button btnUndo   = new Button("Undo");
        Button btnRedo   = new Button("Redo");
        Button btnEraser = new Button("Gomme");

        // --- Radio 2D / 3D ---
        ToggleGroup dimensionGroup = new ToggleGroup();
        RadioButton rb2D = new RadioButton("2D");
        RadioButton rb3D = new RadioButton("3D");
        rb2D.setToggleGroup(dimensionGroup);
        rb3D.setToggleGroup(dimensionGroup);
        rb2D.setSelected(true);

        // --- Logger combo ---
        Label lblLogger = new Label("Logger:");
        comboLogger = new ComboBox<>(
                FXCollections.observableArrayList("LogConsole", "LogFile", "LogDB")
        );
        comboLogger.setValue("LogConsole");

        // --- Actions boutons forme ---
        btnRectangle.setOnAction(e -> {
            eraserMode = false;
            onRectangle.run();
            selectedShapeType = "RECTANGLE";
            sf = is3D ? new RectangleFactory3D() : new RectangleFactory();
            notifyObservers();
        });
        btnCircle.setOnAction(e -> {
            eraserMode = false;
            onCircle.run();
            selectedShapeType = "CIRCLE";
            sf = is3D ? new CircleFactory3D() : new CircleFactory();
            notifyObservers();
        });
        btnLine.setOnAction(e -> {
            eraserMode = false;
            onLine.run();
            selectedShapeType = "LINE";
            sf = is3D ? new LineFactory3D() : new LineFactory();
            notifyObservers();
        });

        // --- Actions commande (Command pattern) ---
        btnUndo.setOnAction(e -> onUndo.run());
        btnRedo.setOnAction(e -> onRedo.run());
        btnEraser.setOnAction(e -> {
            eraserMode = true;
            onEraser.run();
        });

        // --- Actions radio 2D / 3D ---
        rb2D.setOnAction(e -> { is3D = false; updateFactory(); });
        rb3D.setOnAction(e -> { is3D = true;  updateFactory(); });

        comboLogger.setOnAction(e -> onLoggerChange.accept(comboLogger.getValue()));

        // --- Layout ---
        root = new HBox(10);
        root.setPadding(new Insets(10));
        root.setStyle(
                "-fx-background-color: #f4f4f4;" +
                "-fx-border-color: #cccccc;" +
                "-fx-border-width: 0 0 1 0;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);"
        );
        root.getChildren().addAll(
                btnRectangle, btnCircle, btnLine,
                rb2D, rb3D,
                btnUndo, btnRedo, btnEraser,
                lblLogger, comboLogger
        );
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

    public IShape createShape(double startX, double startY, double endX, double endY) {
        if (sf != null) {
            return sf.createShape(startX, startY, endX, endY);
        }
        return null;
    }

    public boolean isEraserMode() { return eraserMode; }

    public HBox getView() { return root; }

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
