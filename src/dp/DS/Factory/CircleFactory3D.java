package dp.DS.Factory;

import dp.DS.observer.IShape;
import dp.DS.observer.CircleShape3D;

/**
 * Factory concrète 3D — crée un CircleShape3D (effet sphère).
 */
public class CircleFactory3D implements ShapeFactory {
    @Override
    public IShape createShape(double startX, double startY, double endX, double endY) {
        double x = Math.min(startX, endX);
        double y = Math.min(startY, endY);
        double width = Math.abs(endX - startX);
        double height = Math.abs(endY - startY);
        return new CircleShape3D(x, y, width, height);
    }
}
