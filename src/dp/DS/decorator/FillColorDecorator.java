package dp.DS.decorator;

import dp.DS.observer.IShape;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Decorator concret — remplit la forme avec une couleur, puis
 * delegue au shape encapsule pour tracer le contour.
 */
public class FillColorDecorator extends ShapeDecorator {

    private final Color fillColor;

    public FillColorDecorator(IShape wrapped, Color fillColor) {
        super(wrapped);
        this.fillColor = fillColor;
    }

    public Color getFillColor() { return fillColor; }

    @Override
    public void draw(GraphicsContext gc) {
        gc.save();
        gc.setFill(fillColor);
        wrapped.fillShape(gc);
        gc.restore();
        wrapped.draw(gc);
    }

    @Override
    public String toString() {
        return wrapped.toString() + " +Fill(" + fillColor + ")";
    }
}
