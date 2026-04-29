package dp.DS.observer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Observer concret — Shape Ligne.
 * Se dessine sur le canvas quand notifié par l'Observable.
 */
public class LineShape implements IShape {

    private final double startX, startY, endX, endY;
    private final List<IObserver> observers = new ArrayList<>();

    public LineShape(double startX, double startY, double endX, double endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
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
        gc.strokeLine(startX, startY, endX, endY);
    }

    @Override
    public String toString() {
        return "LINE from [" + startX + "," + startY + "] to [" + endX + "," + endY + "]";
    }
}
