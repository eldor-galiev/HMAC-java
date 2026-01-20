package ru.yandex.practicum.server;

import com.sun.net.httpserver.HttpServer;
import ru.yandex.practicum.config.AppConfig;
import ru.yandex.practicum.crypto.HmacService;
import ru.yandex.practicum.server.handlers.SignHandler;
import ru.yandex.practicum.server.handlers.VerifyHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class HmacServer {
    private static final Logger LOGGER = Logger.getLogger(HmacServer.class.getName());

    private final HttpServer server;
    private final HmacService hmacService;
    private final AppConfig config;

    public HmacServer(AppConfig config) throws IOException {
        this.config = config;
        this.hmacService = new HmacService(config.getSecret(), config.getHmacAlg());

        this.server = HttpServer.create(
                new InetSocketAddress(config.getListenPort()),
                0
        );

        setupRoutes();

        server.setExecutor(Executors.newFixedThreadPool(10));
    }

    private void setupRoutes() {
        server.createContext("/sign", new SignHandler(hmacService, config));
        server.createContext("/verify", new VerifyHandler(hmacService, config));
    }

    public void start() {
        server.start();
        LOGGER.info(String.format(
                "Server started on port %d. Max message size: %d bytes",
                config.getListenPort(),
                config.getMaxMsgSizeBytes()
        ));
        LOGGER.info("Endpoints available:");
        LOGGER.info("  POST /sign");
        LOGGER.info("  POST /verify");
    }

    public void stop() {
        server.stop(0);
        LOGGER.info("Server stopped");
    }
}