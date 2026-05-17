package dp.DS.observer;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class DrawingCanvas implements IObserver {

    private final Canvas canvas;
    /** Conteneur redimensionnable : le canvas suit sa taille (remplit la fenêtre). */
    private final Pane container;
    private final List<IShape> shapes = new ArrayList<>();

    /** Chemin à surligner (séquence de centres [x,y]) ou null. */
    private List<double[]> highlightPath;

    public DrawingCanvas(double width, double height) {
        this.canvas = new Canvas(width, height);
        this.container = new Pane(canvas);
        // Le canvas occupe toute la place offerte par le conteneur.
        canvas.widthProperty().bind(container.widthProperty());
        canvas.heightProperty().bind(container.heightProperty());
        canvas.widthProperty().addListener((o, a, b) -> redraw());
        canvas.heightProperty().addListener((o, a, b) -> redraw());
        clearCanvas();
    }

    /**
     * Retourne le Canvas JavaFX (pour les événements souris).
     */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Retourne le conteneur redimensionnable à placer dans le layout.
     */
    public Pane getView() {
        return container;
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
        gc.setFill(Color.WHITE);
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
     * Retourne une copie de la liste des formes (utilisée par le module graphe).
     */
    public List<IShape> getShapes() {
        return new ArrayList<>(shapes);
    }

    /**
     * Définit le chemin à surligner et redessine.
     */
    public void setHighlightPath(List<double[]> path) {
        this.highlightPath = path;
        redraw();
    }

    /**
     * Efface le surlignage du chemin (sans redessiner ; l'appelant redessine).
     */
    public void clearHighlight() {
        this.highlightPath = null;
    }

    /**
     * Cherche la forme la plus en haut sous le point (x,y), ou null sinon.
     */
    public IShape findShapeAt(double x, double y) {
        for (int i = shapes.size() - 1; i >= 0; i--) {
            IShape s = shapes.get(i);
            if (s.contains(x, y)) return s;
        }
        return null;
    }

    /**
     * Redessine tout le canvas : efface + dessine toutes les formes.
     */
    public void redraw() {
        clearCanvas();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        for (IShape shape : shapes) {
            gc.save();
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            shape.draw(gc);
            gc.restore();
        }
        drawHighlightPath(gc);
    }

    /**
     * Dessine par-dessus les formes le plus court chemin calculé (en rouge).
     */
    private void drawHighlightPath(GraphicsContext gc) {
        if (highlightPath == null || highlightPath.size() < 2) return;
        gc.save();
        gc.setStroke(Color.RED);
        gc.setLineWidth(4);
        for (int i = 0; i < highlightPath.size() - 1; i++) {
            double[] p = highlightPath.get(i);
            double[] q = highlightPath.get(i + 1);
            gc.strokeLine(p[0], p[1], q[0], q[1]);
        }
        gc.setFill(Color.RED);
        for (double[] p : highlightPath) {
            gc.fillOval(p[0] - 5, p[1] - 5, 10, 10);
        }
        gc.restore();
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
