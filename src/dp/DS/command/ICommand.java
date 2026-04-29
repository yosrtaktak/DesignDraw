package dp.DS.command;

/**
 * Interface Command — chaque commande peut être exécutée et annulée.
 */
public interface ICommand {
    void execute();
    void undo();
}
