package dp.DS.observer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Observer concret — Shape Cercle.
 * Se dessine sur le canvas quand notifié par l'Observable.
 */
public class CircleShape implements IShape {

    private final double x, y, width, height;
    private final List<IObserver> observers = new ArrayList<>();

    public CircleShape(double x, double y, double width, double height) {
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
    public void draw(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeOval(x, y, width, height);
    }

    @Override
    public String toString() {
        return "CIRCLE at [" + x + "," + y + "] size [" + width + "x" + height + "]";
    }
}
