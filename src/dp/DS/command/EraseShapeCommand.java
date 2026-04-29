package dp.DS.command;

import dp.DS.observer.DrawingCanvas;
import dp.DS.observer.IShape;


public class EraseShapeCommand implements ICommand {

    private final DrawingCanvas canvas;
    private final IShape shape;

    public EraseShapeCommand(DrawingCanvas canvas, IShape shape) {
        this.canvas = canvas;
        this.shape = shape;
    }

    @Override
    public void execute() {
        canvas.removeShape(shape);
    }

    @Override
    public void undo() {
        canvas.addShape(shape);
    }
}
