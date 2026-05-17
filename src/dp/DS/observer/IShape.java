package dp.DS.observer;

import javafx.scene.canvas.GraphicsContext;

public interface IShape extends IObservable {
    /**
     * Rendu de base : par défaut rien. Le rendu visible est ajouté par les
     * décorateurs — FillColorDecorator (remplissage) et BorderColorDecorator
     * (contour). Une forme sans aucun décorateur n'affiche donc rien.
     */
    default void draw(GraphicsContext gc) { }

    /** Remplit l'intérieur de la forme (utilisé par FillColorDecorator). */
    default void fillShape(GraphicsContext gc) { }

    /** Trace le contour de la forme (utilisé par BorderColorDecorator). */
    default void strokeShape(GraphicsContext gc) { }

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
