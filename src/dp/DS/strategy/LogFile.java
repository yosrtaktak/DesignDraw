package dp.DS.strategy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Stratégie de logging vers un fichier.
 * Écrit les messages dans le fichier logs/application.log avec un timestamp.
 */
public class LogFile implements ILogger {

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = LOG_DIR + "/application.log";
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LogFile() {
        // Créer le répertoire logs s'il n'existe pas
        File dir = new File(LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public void log(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = "[FILE] [" + timestamp + "] " + message;

        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println(logEntry);
        } catch (IOException e) {
            System.err.println("Erreur d'écriture dans le fichier log: " + e.getMessage());
        }

        // Afficher aussi dans la console pour confirmation
        System.out.println(logEntry + " (écrit dans " + LOG_FILE + ")");
    }
}
