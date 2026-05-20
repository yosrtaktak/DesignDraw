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
 * Forme Rectangle 3D — dessinée avec un dégradé et une ombre
 * pour simuler un effet tridimensionnel.
 */
public class RectangleShape3D implements IShape {

    private double x, y, width, height;
    private final List<IObserver> observers = new ArrayList<>();

    public RectangleShape3D(double x, double y, double width, double height) {
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
     * Corps 3D : dégradé dérivé de la couleur de remplissage choisie
     * (appliquée par FillColorDecorator via gc.setFill).
     */
    @Override
    public void fillShape(GraphicsContext gc) {
        Color base = (gc.getFill() instanceof Color)
                ? (Color) gc.getFill() : Color.rgb(0, 140, 110);
        gc.save();
        gc.setEffect(new DropShadow(10, 5, 5, Color.rgb(0, 0, 0, 0.5)));
        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, base.brighter()),
                new Stop(0.5, base),
                new Stop(1, base.darker())
        );
        gc.setFill(gradient);
        gc.fillRect(x, y, width, height);
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
        gc.strokeRect(x, y, width, height);
        gc.restore();
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
        return "RECTANGLE_3D at [" + x + "," + y + "] size [" + width + "x" + height + "]";
    }
}
