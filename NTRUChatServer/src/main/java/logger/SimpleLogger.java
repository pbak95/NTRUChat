package main.java.logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by Patryk on 30.03.2018.
 */
public class SimpleLogger {

    private static SimpleLogger logger;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private SimpleLogger(){};

    public void logMessage(String message) {
        System.out.println("[" + LocalDateTime.now().format(TIME_FORMATTER) + "] " + message);
    }

    public static SimpleLogger getInstance() {
        if (logger != null) {
            return logger;
        } else {
            logger = new SimpleLogger();
            return logger;
        }
    }
}
