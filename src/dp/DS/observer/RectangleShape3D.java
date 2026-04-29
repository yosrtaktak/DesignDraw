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

    private final double x, y, width, height;
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

    @Override
    public void draw(GraphicsContext gc) {
        gc.save();

        // Ombre portée pour effet 3D
        gc.setEffect(new DropShadow(10, 5, 5, Color.rgb(0, 0, 0, 0.5)));

        // Dégradé linéaire pour simuler la profondeur
        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(0, 180, 140)),
                new Stop(0.5, Color.rgb(0, 140, 110)),
                new Stop(1, Color.rgb(0, 100, 80))
        );

        gc.setFill(gradient);
        gc.fillRect(x, y, width, height);

        gc.setStroke(Color.rgb(0, 80, 60));
        gc.setLineWidth(1.5);
        gc.strokeRect(x, y, width, height);

        gc.restore();
    }

    @Override
    public String toString() {
        return "RECTANGLE_3D at [" + x + "," + y + "] size [" + width + "x" + height + "]";
    }
}
