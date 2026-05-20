package dp.DS.singleton;

import dp.DS.strategy.ILogger;
import dp.DS.strategy.LogConsole;

/**
 * Logger Singleton qui utilise le Strategy Pattern.
 * 
 * - Singleton : une seule instance dans toute l'application (constructeur privé).
 * - Strategy  : délègue le logging à une stratégie ILogger interchangeable.
 * 
 * Par défaut, la stratégie est LogConsole.
 * On peut changer la stratégie à runtime avec setStrategy().
 */
public class Logger {

    // Stratégie de logging courante (Strategy Pattern)
    private ILogger strategy;

    /**
     * Constructeur privé (Singleton).
     * Initialise avec LogConsole par défaut.
     */
    private Logger() {
        this.strategy = new LogConsole();
    }

    /**
     * Holder idiom : la classe interne n'est chargée qu'au premier appel à
     * getInstance(). Le JVM garantit qu'une classe n'est initialisée qu'une
     * seule fois et de manière thread-safe — donc INSTANCE est lazy ET unique
     * sans synchronized ni volatile.
     */
    private static class Holder {
        private static final Logger INSTANCE = new Logger();
    }

    /**
     * Retourne l'instance unique du Logger (lazy, thread-safe).
     *
     * @return l'instance unique du Logger
     */
    public static Logger getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Change la stratégie de logging à runtime.
     * 
     * @param strategy la nouvelle stratégie de logging
     */
    public void setStrategy(ILogger strategy) {
        this.strategy = strategy;
    }

    /**
     * Retourne la stratégie de logging courante.
     * 
     * @return la stratégie courante
     */
    public ILogger getStrategy() {
        return this.strategy;
    }

    /**
     * Enregistre un message de log en utilisant la stratégie courante.
     * 
     * @param message le message à logger
     */
    public void log(String message) {
        strategy.log(message);
    }
}
