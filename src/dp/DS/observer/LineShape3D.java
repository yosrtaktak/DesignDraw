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

    private final double startX, startY, endX, endY;
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

    @Override
    public void draw(GraphicsContext gc) {
        gc.save();

        // Ombre portée
        gc.setEffect(new DropShadow(6, 3, 3, Color.rgb(0, 0, 0, 0.4)));

        // Ligne épaisse avec dégradé simulé (trait principal + trait clair)
        gc.setStroke(Color.rgb(60, 60, 180));
        gc.setLineWidth(4);
        gc.strokeLine(startX, startY, endX, endY);

        // Reflet clair au-dessus
        gc.setEffect(null);
        gc.setStroke(Color.rgb(160, 180, 255, 0.6));
        gc.setLineWidth(1.5);
        gc.strokeLine(startX, startY - 1, endX, endY - 1);

        gc.restore();
    }

    @Override
    public String toString() {
        return "LINE_3D from [" + startX + "," + startY + "] to [" + endX + "," + endY + "]";
    }
}
