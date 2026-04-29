package dp.DS.Factory;

import dp.DS.observer.IShape;
import dp.DS.observer.LineShape;

/**
 * Factory concrète 2D — crée un LineShape (forme plate).
 */
public class LineFactory implements ShapeFactory {
    @Override
    public IShape createShape(double startX, double startY, double endX, double endY) {
        return new LineShape(startX, startY, endX, endY);
    }
}
