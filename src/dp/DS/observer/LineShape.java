package dp.DS.observer;

import javafx.scene.canvas.GraphicsContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Observer concret — Shape Ligne.
 * Se dessine sur le canvas quand notifié par l'Observable.
 */
public class LineShape implements IShape {

    private double startX, startY, endX, endY;
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
    public void strokeShape(GraphicsContext gc) {
        gc.strokeLine(startX, startY, endX, endY);
    }

    @Override
    public boolean contains(double px, double py) {
        // Distance from point to line segment, with a tolerance for picking.
        double dx = endX - startX;
        double dy = endY - startY;
        double len2 = dx * dx + dy * dy;
        if (len2 == 0) {
            double ex = px - startX, ey = py - startY;
            return ex * ex + ey * ey <= 36; // 6px radius
        }
        double t = ((px - startX) * dx + (py - startY) * dy) / len2;
        t = Math.max(0, Math.min(1, t));
        double projX = startX + t * dx;
        double projY = startY + t * dy;
        double ex = px - projX, ey = py - projY;
        return ex * ex + ey * ey <= 36; // 6px tolerance
    }

    @Override
    public void resize(double newEndX, double newEndY) {
        this.endX = newEndX;
        this.endY = newEndY;
        notifyObservers();
    }

    @Override
    public void resizeTo(double size) {
        double s = Math.max(1, size);
        double dx = endX - startX;
        double dy = endY - startY;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len == 0) {
            this.endX = startX + s;
            this.endY = startY;
        } else {
            this.endX = startX + dx / len * s;
            this.endY = startY + dy / len * s;
        }
        notifyObservers();
    }

    @Override
    public double getEndX() { return endX; }

    @Override
    public double getEndY() { return endY; }

    @Override
    public String shapeKind() { return "LINE"; }

    @Override
    public double getStartX() { return startX; }

    @Override
    public double getStartY() { return startY; }

    @Override
    public String toString() {
        return "LINE from [" + startX + "," + startY + "] to [" + endX + "," + endY + "]";
    }
}
