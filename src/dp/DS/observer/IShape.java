package dp.DS.observer;

import javafx.scene.canvas.GraphicsContext;

public interface IShape extends IObservable {
    void draw(GraphicsContext gc);

    default void fillShape(GraphicsContext gc) { }

    default boolean contains(double x, double y) { return false; }

    default void resize(double endX, double endY) { }

    /** Redimensionne la forme a une taille absolue (cote / longueur). */
    default void resizeTo(double size) { }

    default double getEndX() { return 0; }

    default double getEndY() { return 0; }

    // --- Accesseurs utilisés par le module graphe (plus court chemin) ---

    /** Type logique de la forme : "CIRCLE", "LINE", "RECTANGLE" ou "SHAPE". */
    default String shapeKind() { return "SHAPE"; }

    /** true si la forme est une variante 3D (dégradé + ombre). */
    default boolean is3D() { return false; }

    default double getCenterX() { return 0; }

    default double getCenterY() { return 0; }

    default double getStartX() { return 0; }

    default double getStartY() { return 0; }
}
