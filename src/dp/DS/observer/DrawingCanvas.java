package dp.DS.observer;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class DrawingCanvas implements IObserver {

    private final Canvas canvas;
    private final List<IShape> shapes = new ArrayList<>();

    public DrawingCanvas(double width, double height) {
        this.canvas = new Canvas(width, height);
        clearCanvas();
    }

    /**
     * Retourne le Canvas JavaFX pour l'intégrer dans le layout.
     */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Retourne le GraphicsContext pour le dessin.
     */
    public GraphicsContext getGraphicsContext() {
        return canvas.getGraphicsContext2D();
    }

    /**
     * Efface le canvas et redessine le fond jaune.
     */
    public void clearCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.YELLOW);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void addShape(IShape shape) {
        shapes.add(shape);
        shape.addObserver(this); // Canvas observe cette forme
        shape.notifyObservers(); // Demande une mise à jour suite à l'ajout
    }

    public void removeLastShape() {
        if (!shapes.isEmpty()) {
            IShape shape = shapes.remove(shapes.size() - 1);
            shape.removeObserver(this);
            this.update();
        }
    }

    public void clearShapes() {
        for (IShape s : shapes) s.removeObserver(this);
        shapes.clear();
        this.update();
    }

    @Override
    public void update() {
        clearCanvas();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        for (IShape shape : shapes) {
            shape.draw(gc);
        }
    }

    public int getShapeCount() {
        return shapes.size();
    }
}
