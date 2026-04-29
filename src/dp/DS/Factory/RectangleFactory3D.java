package dp.DS.Factory;

import dp.DS.observer.IShape;
import dp.DS.observer.RectangleShape3D;

/**
 * Factory concrète 3D — crée un RectangleShape3D (avec dégradé et ombre).
 */
public class RectangleFactory3D implements ShapeFactory {
    @Override
    public IShape createShape(double startX, double startY, double endX, double endY) {
        double x = Math.min(startX, endX);
        double y = Math.min(startY, endY);
        double width = Math.abs(endX - startX);
        double height = Math.abs(endY - startY);
        return new RectangleShape3D(x, y, width, height);
    }
}
