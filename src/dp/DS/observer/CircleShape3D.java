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

    private final double x, y, width, height;
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

    @Override
    public void draw(GraphicsContext gc) {
        gc.save();

        // Ombre portée pour effet 3D
        gc.setEffect(new DropShadow(10, 4, 4, Color.rgb(0, 0, 0, 0.4)));

        // Dégradé radial pour simuler une sphère
        RadialGradient gradient = new RadialGradient(
                0, 0,
                x + width * 0.35, y + height * 0.35,   // centre du reflet (en haut à gauche)
                Math.max(width, height) * 0.6,           // rayon
                false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(180, 210, 255)),   // reflet clair
                new Stop(0.4, Color.rgb(100, 150, 255)), // bleu moyen
                new Stop(1, Color.rgb(40, 70, 160))      // bleu foncé
        );

        gc.setFill(gradient);
        gc.fillOval(x, y, width, height);

        gc.restore();
    }

    @Override
    public String toString() {
        return "CIRCLE_3D at [" + x + "," + y + "] size [" + width + "x" + height + "]";
    }
}
