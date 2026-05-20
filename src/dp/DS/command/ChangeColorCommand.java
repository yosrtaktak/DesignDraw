package dp.DS.command;

import dp.DS.observer.DrawingCanvas;
import dp.DS.observer.IShape;

/**
 * Command — change la couleur (remplissage / bordure) d'une forme existante,
 * et permet aussi de retirer ou d'ajouter la bordure.
 *
 * La "nouvelle" forme est un nouvel empilement de Decorators autour de la
 * meme forme brute (voir {@code ToolPalette#recolor}). L'execution remplace
 * l'ancien empilement par le nouveau dans le canvas ; l'undo restaure
 * l'ancien empilement. La position de la forme dans la liste (z-order) est
 * preservee par {@code DrawingCanvas#replaceShape}.
 */
public class ChangeColorCommand implements ICommand {

    private final DrawingCanvas canvas;
    private final IShape oldShape;
    private final IShape newShape;

    public ChangeColorCommand(DrawingCanvas canvas, IShape oldShape, IShape newShape) {
        this.canvas = canvas;
        this.oldShape = oldShape;
        this.newShape = newShape;
    }

    @Override
    public void execute() {
        canvas.replaceShape(oldShape, newShape);
    }

    @Override
    public void undo() {
        canvas.replaceShape(newShape, oldShape);
    }
}
