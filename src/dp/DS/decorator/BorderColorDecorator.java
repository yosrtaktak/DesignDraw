package dp.DS.decorator;

import dp.DS.observer.IShape;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Decorator concret — applique une couleur de bordure (stroke)
 * avant de tracer la forme encapsulee.
 */
public class BorderColorDecorator extends ShapeDecorator {

    private final Color borderColor;

    public BorderColorDecorator(IShape wrapped, Color borderColor) {
        super(wrapped);
        this.borderColor = borderColor;
    }

    public Color getBorderColor() { return borderColor; }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(borderColor);
        wrapped.draw(gc);
    }

    @Override
    public String toString() {
        return wrapped.toString() + " +Border(" + borderColor + ")";
    }
}
