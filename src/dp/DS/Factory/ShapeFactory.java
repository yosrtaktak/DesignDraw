package dp.DS.Factory;

import dp.DS.observer.IShape;

public interface ShapeFactory {
    IShape createShape( double startX, double startY, double endX, double endY);
}
