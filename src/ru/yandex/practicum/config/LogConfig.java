package ru.yandex.practicum.config;

import java.io.IOException;
import java.util.logging.*;

public class LogConfig {

    public static void setup() {
        setup("hmac-server.log");
    }

    public static void setup(String logFile) {
        System.setProperty("java.util.logging.FileHandler.lock", "false");

        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }

        try {
            setupHandler(new FileHandler(logFile, 0, 1, true), Level.ALL, rootLogger);
            setupHandler(new ConsoleHandler(), Level.INFO, rootLogger);
            rootLogger.setLevel(Level.ALL);
            rootLogger.info("Logging initialized to file: " + logFile);
        } catch (IOException e) {
            System.err.println("Failed to setup file logging: " + e.getMessage());
            setupConsoleOnly();
        }
    }

    private static void setupHandler(Handler handler, Level level, Logger logger) {
        handler.setFormatter(new SimpleFormatter() {
            private static final String FORMAT = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

            @Override
            public synchronized String format(LogRecord record) {
                return String.format(FORMAT,
                        new java.util.Date(record.getMillis()),
                        record.getLevel().getLocalizedName(),
                        formatMessage(record)
                );
            }
        });
        handler.setLevel(level);
        logger.addHandler(handler);
    }

    private static void setupConsoleOnly() {
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }

        setupHandler(new ConsoleHandler(), Level.INFO, rootLogger);
        rootLogger.setLevel(Level.ALL);
        rootLogger.warning("File logging failed, using console only");
    }
}