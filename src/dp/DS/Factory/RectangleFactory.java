package dp.DS.Factory;

import dp.DS.observer.IShape;
import dp.DS.observer.RectangleShape;

/**
 * Factory concrète 2D — crée un RectangleShape (forme plate).
 */
public class RectangleFactory implements ShapeFactory {
    @Override
    public IShape createShape(double startX, double startY, double endX, double endY) {
        double x = Math.min(startX, endX);
        double y = Math.min(startY, endY);
        double width = Math.abs(endX - startX);
        double height = Math.abs(endY - startY);
        return new RectangleShape(x, y, width, height);
    }
}
