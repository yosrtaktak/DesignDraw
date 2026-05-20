package dp.DS.command;

import dp.DS.observer.IShape;

/**
 * Command — redimensionne une forme a une taille absolue choisie dans la
 * palette. Capture l'extremite courante a la construction pour pouvoir annuler.
 *
 * Pas besoin de référence au canvas : la forme notifie ses observateurs
 * (Observer pattern) et le canvas se redessine tout seul.
 */
public class ResizeShapeCommand implements ICommand {

    private final IShape shape;
    private final double newSize;
    private final double oldEndX, oldEndY;

    public ResizeShapeCommand(IShape shape, double newSize) {
        this.shape = shape;
        this.newSize = newSize;
        this.oldEndX = shape.getEndX();
        this.oldEndY = shape.getEndY();
    }

    @Override
    public void execute() {
        // Le redraw est déclenché par l'Observer (shape -> canvas) via notifyObservers().
        shape.resizeTo(newSize);
    }

    @Override
    public void undo() {
        shape.resize(oldEndX, oldEndY);
    }
}
