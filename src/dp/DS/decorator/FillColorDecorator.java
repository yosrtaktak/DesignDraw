package dp.DS.decorator;

import dp.DS.observer.IShape;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Decorator concret — ajoute un remplissage coloré par-dessus ce que
 * dessine déjà la chaîne encapsulée.
 *
 * Conforme au contrat Decorator : on appelle d'abord wrapped.draw(gc)
 * (les couches en-dessous se peignent), puis on ajoute notre contribution.
 * Le résultat est composable dans n'importe quel ordre — seul le z-order
 * (qui est devant) dépend de l'empilement, ce qui est exactement le rôle
 * du pattern.
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
        wrapped.draw(gc);
        gc.save();
        gc.setFill(fillColor);
        wrapped.fillShape(gc);
        gc.restore();
    }

    @Override
    public String toString() {
        return wrapped.toString() + " +Fill(" + fillColor + ")";
    }
}
