package dp.DS.Factory;

import dp.DS.observer.IShape;
import dp.DS.observer.LineShape3D;

/**
 * Factory concrète 3D — crée un LineShape3D (ligne avec ombre).
 */
public class LineFactory3D implements ShapeFactory {
    @Override
    public IShape createShape(double startX, double startY, double endX, double endY) {
        return new LineShape3D(startX, startY, endX, endY);
    }
}
