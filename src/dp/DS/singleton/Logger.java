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

    // Instance unique (Singleton)
    private static Logger instance;

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
     * Retourne l'instance unique du Logger.
     * Crée l'instance au premier appel (lazy initialization).
     * 
     * @return l'instance unique du Logger
     */
    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
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
