package ru.yandex.practicum.server.handlers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.practicum.common.exceptions.CryptoException;
import ru.yandex.practicum.config.AppConfig;
import ru.yandex.practicum.crypto.HmacService;

import java.io.IOException;

public class SignHandler extends BaseHandler {
    private final HmacService hmacService;

    public SignHandler(HmacService hmacService, AppConfig config) {
        super(config);
        this.hmacService = hmacService;
    }

    @Override
    protected void processRequest(HttpExchange exchange, String requestBody) throws IOException {
        JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();

        if (!json.has("msg") || !json.get("msg").isJsonPrimitive()) {
            sendError(exchange, 400, "Missing or invalid 'msg' field");
            return;
        }

        String message = json.get("msg").getAsString();
        if (message == null || message.trim().isEmpty()) {
            sendError(exchange, 400, "Message cannot be empty");
            return;
        }

        try {
            String signature = hmacService.sign(message);
            JsonObject response = new JsonObject();
            response.addProperty("signature", signature);

            sendResponse(exchange, 200, response);
            LOGGER.info(String.format("Signed message of length %d", message.length()));

        } catch (CryptoException e) {
            LOGGER.severe("Crypto error: " + e.getMessage());
            sendError(exchange, 500, "Internal Server Error");
        }
    }
}