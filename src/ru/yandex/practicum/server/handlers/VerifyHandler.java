package ru.yandex.practicum.server.handlers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.practicum.common.exceptions.CryptoException;
import ru.yandex.practicum.config.AppConfig;
import ru.yandex.practicum.crypto.Base64UrlCodec;
import ru.yandex.practicum.crypto.HmacService;

import java.io.IOException;

public class VerifyHandler extends BaseHandler {
    private final HmacService hmacService;

    public VerifyHandler(HmacService hmacService, AppConfig config) {
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

        if (!json.has("signature") || !json.get("signature").isJsonPrimitive()) {
            sendError(exchange, 400, "Missing or invalid 'signature' field");
            return;
        }

        String message = json.get("msg").getAsString();
        String signature = json.get("signature").getAsString();

        if (message == null || message.trim().isEmpty()) {
            sendError(exchange, 400, "Message cannot be empty");
            return;
        }

        if (!Base64UrlCodec.isValidBase64Url(signature)) {
            sendError(exchange, 400, "Invalid signature format");
            return;
        }

        try {
            boolean isValid = hmacService.verify(message, signature);
            JsonObject response = new JsonObject();
            response.addProperty("ok", isValid);

            sendResponse(exchange, 200, response);
            LOGGER.info(String.format(
                "Verification %s for message length %d",
                isValid ? "successful" : "failed",
                message.length()
            ));

        } catch (CryptoException e) {
            LOGGER.severe("Crypto error: " + e.getMessage());
            sendError(exchange, 500, "Internal Server Error");
        }
    }
}