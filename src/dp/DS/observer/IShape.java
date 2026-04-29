package dp.DS.observer;

import javafx.scene.canvas.GraphicsContext;

public interface IShape extends IObservable {
    void draw(GraphicsContext gc);
}

