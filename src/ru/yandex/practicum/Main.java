package ru.yandex.practicum;

import ru.yandex.practicum.config.AppConfig;
import ru.yandex.practicum.config.LogConfig;
import ru.yandex.practicum.server.HmacServer;

public class Main {
    public static void main(String[] args) {
        try {
            AppConfig config = AppConfig.load();
            LogConfig.setup();
            HmacServer server = new HmacServer(config);
            server.start();
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            System.exit(1);
        }
    }
}