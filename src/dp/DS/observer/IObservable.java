package dp.DS.observer;

public interface IObservable {

    void addObserver(IObserver observer);

    void removeObserver(IObserver observer);

    void notifyObservers();
}
