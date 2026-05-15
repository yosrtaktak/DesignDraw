package dp.DS.command;

import dp.DS.observer.DrawingCanvas;
import dp.DS.observer.IShape;

/**
 * Command — redimensionne une forme a une taille absolue choisie dans la
 * palette. Capture l'extremite courante a la construction pour pouvoir annuler.
 */
public class ResizeShapeCommand implements ICommand {

    private final DrawingCanvas canvas;
    private final IShape shape;
    private final double newSize;
    private final double oldEndX, oldEndY;

    public ResizeShapeCommand(DrawingCanvas canvas, IShape shape, double newSize) {
        this.canvas = canvas;
        this.shape = shape;
        this.newSize = newSize;
        this.oldEndX = shape.getEndX();
        this.oldEndY = shape.getEndY();
    }

    @Override
    public void execute() {
        shape.resizeTo(newSize);
        canvas.redraw();
    }

    @Override
    public void undo() {
        shape.resize(oldEndX, oldEndY);
        canvas.redraw();
    }
}
