package dp.DS.observer;

import javafx.scene.canvas.GraphicsContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Observer concret — Shape Rectangle.
 * Se dessine sur le canvas quand notifié par l'Observable.
 */
public class RectangleShape implements IShape {

    private double x, y, width, height;
    private final List<IObserver> observers = new ArrayList<>();

    public RectangleShape(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void addObserver(IObserver observer) { observers.add(observer); }
    @Override
    public void removeObserver(IObserver observer) { observers.remove(observer); }
    @Override
    public void notifyObservers() { for (IObserver obs : observers) obs.update(); }

    @Override
    public void strokeShape(GraphicsContext gc) {
        gc.strokeRect(x, y, width, height);
    }

    @Override
    public void fillShape(GraphicsContext gc) {
        gc.fillRect(x, y, width, height);
    }

    @Override
    public boolean contains(double px, double py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }

    @Override
    public void resize(double endX, double endY) {
        this.width = Math.max(1, endX - x);
        this.height = Math.max(1, endY - y);
        notifyObservers();
    }

    @Override
    public void resizeTo(double size) {
        double s = Math.max(1, size);
        this.width = s;
        this.height = s;
        notifyObservers();
    }

    @Override
    public double getEndX() { return x + width; }

    @Override
    public double getEndY() { return y + height; }

    @Override
    public String shapeKind() { return "RECTANGLE"; }

    @Override
    public double getCenterX() { return x + width / 2.0; }

    @Override
    public double getCenterY() { return y + height / 2.0; }

    @Override
    public double getStartX() { return x; }

    @Override
    public double getStartY() { return y; }

    @Override
    public String toString() {
        return "RECTANGLE at [" + x + "," + y + "] size [" + width + "x" + height + "]";
    }
}
