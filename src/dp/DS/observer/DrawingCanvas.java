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

    /**
     * Ajoute une forme et redessine le canvas.
     */
    public void addShape(IShape shape) {
        shapes.add(shape);
        shape.addObserver(this);
        redraw();
    }

    /**
     * Retire une forme spécifique et redessine (utilisé par les commandes).
     */
    public void removeShape(IShape shape) {
        shapes.remove(shape);
        shape.removeObserver(this);
        redraw();
    }

    /**
     * Retourne la dernière forme ajoutée (pour la gomme).
     */
    public IShape getLastShape() {
        if (shapes.isEmpty()) return null;
        return shapes.get(shapes.size() - 1);
    }

    /**
     * Redessine tout le canvas : efface + dessine toutes les formes.
     */
    public void redraw() {
        clearCanvas();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        for (IShape shape : shapes) {
            shape.draw(gc);
        }
    }

    public void clearShapes() {
        for (IShape s : shapes) s.removeObserver(this);
        shapes.clear();
        redraw();
    }

    @Override
    public void update() {
        redraw();
    }

    public int getShapeCount() {
        return shapes.size();
    }
}
