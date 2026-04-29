package dp.DS.command;

import dp.DS.observer.DrawingCanvas;
import dp.DS.observer.IShape;


public class AddShapeCommand implements ICommand {

    private final DrawingCanvas canvas;
    private final IShape shape;

    public AddShapeCommand(DrawingCanvas canvas, IShape shape) {
        this.canvas = canvas;
        this.shape = shape;
    }

    @Override
    public void execute() {
        canvas.addShape(shape);
    }

    @Override
    public void undo() {
        canvas.removeShape(shape);
    }
}
