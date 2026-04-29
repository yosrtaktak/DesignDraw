package dp.DS.Factory;

import dp.DS.observer.IShape;

/**
 * Interface Factory Method — chaque factory concrète
 * crée un seul type de forme via createShape().
 */
public interface ShapeFactory {
    IShape createShape(double startX, double startY, double endX, double endY);
}
