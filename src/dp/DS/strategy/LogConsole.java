package dp.DS.strategy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogConsole implements ILogger {

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void log(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("[CONSOLE] [" + timestamp + "] " + message);
    }
}
