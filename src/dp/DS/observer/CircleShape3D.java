package dp.DS.observer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Forme Cercle 3D — dessinée avec un dégradé radial
 * pour simuler une sphère.
 */
public class CircleShape3D implements IShape {

    private double x, y, width, height;
    private final List<IObserver> observers = new ArrayList<>();

    public CircleShape3D(double x, double y, double width, double height) {
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

    /**
     * Corps 3D (sphère) : dégradé radial dérivé de la couleur de
     * remplissage choisie (appliquée par FillColorDecorator).
     */
    @Override
    public void fillShape(GraphicsContext gc) {
        Color base = (gc.getFill() instanceof Color)
                ? (Color) gc.getFill() : Color.rgb(100, 150, 255);
        gc.save();
        gc.setEffect(new DropShadow(10, 4, 4, Color.rgb(0, 0, 0, 0.4)));
        RadialGradient gradient = new RadialGradient(
                0, 0,
                x + width * 0.35, y + height * 0.35,   // centre du reflet (en haut à gauche)
                Math.max(width, height) * 0.6,           // rayon
                false, CycleMethod.NO_CYCLE,
                new Stop(0, base.brighter().brighter()), // reflet clair
                new Stop(0.4, base),                     // couleur choisie
                new Stop(1, base.darker())               // ombre
        );
        gc.setFill(gradient);
        gc.fillOval(x, y, width, height);
        gc.restore();
    }

    /**
     * Contour : utilise la couleur de bordure choisie
     * (appliquée par BorderColorDecorator via gc.setStroke).
     */
    @Override
    public void strokeShape(GraphicsContext gc) {
        gc.save();
        gc.setLineWidth(1.5);
        gc.strokeOval(x, y, width, height);
        gc.restore();
    }

    @Override
    public boolean contains(double px, double py) {
        if (width <= 0 || height <= 0) return false;
        double cx = x + width / 2.0;
        double cy = y + height / 2.0;
        double rx = width / 2.0;
        double ry = height / 2.0;
        double dx = (px - cx) / rx;
        double dy = (py - cy) / ry;
        return dx * dx + dy * dy <= 1.0;
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
    public String shapeKind() { return "CIRCLE"; }

    @Override
    public boolean is3D() { return true; }

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
        return "CIRCLE_3D at [" + x + "," + y + "] size [" + width + "x" + height + "]";
    }
}
