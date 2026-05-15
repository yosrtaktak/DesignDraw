package dp.DS.decorator;

import dp.DS.observer.IObserver;
import dp.DS.observer.IShape;
import javafx.scene.canvas.GraphicsContext;

/**
 * Decorator abstrait — enveloppe un IShape et delegue par defaut
 * toutes les operations a l'objet decore. Les sous-classes
 * ajoutent un comportement avant/apres l'appel a wrapped.
 */
public abstract class ShapeDecorator implements IShape {

    protected final IShape wrapped;

    protected ShapeDecorator(IShape wrapped) {
        this.wrapped = wrapped;
    }

    /** Forme encapsulée (utilisé pour parcourir la chaîne de décorateurs). */
    public IShape getWrapped() { return wrapped; }

    @Override
    public void draw(GraphicsContext gc) {
        wrapped.draw(gc);
    }

    @Override
    public void fillShape(GraphicsContext gc) {
        wrapped.fillShape(gc);
    }

    @Override
    public boolean contains(double x, double y) { return wrapped.contains(x, y); }

    @Override
    public void resize(double endX, double endY) { wrapped.resize(endX, endY); }

    @Override
    public void resizeTo(double size) { wrapped.resizeTo(size); }

    @Override
    public double getEndX() { return wrapped.getEndX(); }

    @Override
    public double getEndY() { return wrapped.getEndY(); }

    @Override
    public String shapeKind() { return wrapped.shapeKind(); }

    @Override
    public boolean is3D() { return wrapped.is3D(); }

    @Override
    public double getCenterX() { return wrapped.getCenterX(); }

    @Override
    public double getCenterY() { return wrapped.getCenterY(); }

    @Override
    public double getStartX() { return wrapped.getStartX(); }

    @Override
    public double getStartY() { return wrapped.getStartY(); }

    @Override
    public void addObserver(IObserver observer) { wrapped.addObserver(observer); }

    @Override
    public void removeObserver(IObserver observer) { wrapped.removeObserver(observer); }

    @Override
    public void notifyObservers() { wrapped.notifyObservers(); }

    @Override
    public String toString() { return wrapped.toString(); }
}
