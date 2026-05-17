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
        // Rend d'abord le contenu encapsulé (le remplissage si un
        // FillColorDecorator est dessous), puis ajoute le contour.
        wrapped.draw(gc);
        gc.save();
        gc.setStroke(borderColor);
        gc.setLineWidth(2);
        wrapped.strokeShape(gc);
        gc.restore();
    }

    @Override
    public String toString() {
        return wrapped.toString() + " +Border(" + borderColor + ")";
    }
}
