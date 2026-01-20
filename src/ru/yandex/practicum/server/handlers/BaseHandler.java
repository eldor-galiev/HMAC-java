package ru.yandex.practicum.server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.config.AppConfig;
import ru.yandex.practicum.server.dto.ErrorResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public abstract class BaseHandler implements HttpHandler {
    protected static final Gson GSON = new Gson();
    protected static final Logger LOGGER = Logger.getLogger(BaseHandler.class.getName());

    protected final AppConfig config;

    protected BaseHandler(AppConfig config) {
        this.config = config;
}

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (!"application/json".equals(contentType)) {
                sendError(exchange, 415, "Unsupported Media Type");
                return;
            }

            String requestBody = readRequestBody(exchange);
            if (requestBody.length() > config.getMaxMsgSizeBytes()) {
                sendError(exchange, 413, "Payload Too Large");
                return;
            }

            processRequest(exchange, requestBody);

        } catch (JsonSyntaxException e) {
            sendError(exchange, 400, "Invalid JSON format");
        } catch (Exception e) {
            LOGGER.severe("Internal server error: " + e.getMessage());
            sendError(exchange, 500, "Internal Server Error");
        }
    }

    protected abstract void processRequest(HttpExchange exchange, String requestBody) throws IOException;

    protected String readRequestBody(HttpExchange exchange) throws IOException {
        StringBuilder body = new StringBuilder();
        try (InputStream is = exchange.getRequestBody();
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                body.append(line);
            }
        }
        return body.toString();
    }

    protected void sendResponse(HttpExchange exchange, int statusCode, Object response) throws IOException {
        String jsonResponse = GSON.toJson(response);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes(StandardCharsets.UTF_8).length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
        }
    }

    protected void sendError(HttpExchange exchange, int statusCode, String error) throws IOException {
        ErrorResponse response = new ErrorResponse(error.toLowerCase().replace(" ", "_"));
        sendResponse(exchange, statusCode, response);
        LOGGER.warning(String.format("Error %d: %s", statusCode, error));
    }
}