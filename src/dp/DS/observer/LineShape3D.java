package dp.DS.observer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Forme Ligne 3D — dessinée avec un dégradé et une ombre
 * pour simuler un effet de profondeur.
 */
public class LineShape3D implements IShape {

    private double startX, startY, endX, endY;
    private final List<IObserver> observers = new ArrayList<>();

    public LineShape3D(double startX, double startY, double endX, double endY) {
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

    /**
     * Trait 3D dérivé de la couleur de bordure choisie
     * (appliquée par BorderColorDecorator via gc.setStroke).
     */
    @Override
    public void strokeShape(GraphicsContext gc) {
        Color base = (gc.getStroke() instanceof Color)
                ? (Color) gc.getStroke() : Color.rgb(60, 60, 180);
        gc.save();

        // Ombre portée + trait principal de la couleur choisie
        gc.setEffect(new DropShadow(6, 3, 3, Color.rgb(0, 0, 0, 0.4)));
        gc.setStroke(base);
        gc.setLineWidth(4);
        gc.strokeLine(startX, startY, endX, endY);

        // Reflet clair au-dessus (variante claire de la couleur choisie)
        gc.setEffect(null);
        gc.setStroke(base.brighter());
        gc.setLineWidth(1.5);
        gc.strokeLine(startX, startY - 1, endX, endY - 1);

        gc.restore();
    }

    @Override
    public boolean contains(double px, double py) {
        double dx = endX - startX;
        double dy = endY - startY;
        double len2 = dx * dx + dy * dy;
        if (len2 == 0) {
            double ex = px - startX, ey = py - startY;
            return ex * ex + ey * ey <= 36;
        }
        double t = ((px - startX) * dx + (py - startY) * dy) / len2;
        t = Math.max(0, Math.min(1, t));
        double projX = startX + t * dx;
        double projY = startY + t * dy;
        double ex = px - projX, ey = py - projY;
        return ex * ex + ey * ey <= 36;
    }

    @Override
    public void resize(double newEndX, double newEndY) {
        this.endX = newEndX;
        this.endY = newEndY;
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
    }

    @Override
    public double getEndX() { return endX; }

    @Override
    public double getEndY() { return endY; }

    @Override
    public String shapeKind() { return "LINE"; }

    @Override
    public boolean is3D() { return true; }

    @Override
    public double getStartX() { return startX; }

    @Override
    public double getStartY() { return startY; }

    @Override
    public String toString() {
        return "LINE_3D from [" + startX + "," + startY + "] to [" + endX + "," + endY + "]";
    }
}
