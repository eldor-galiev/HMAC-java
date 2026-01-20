package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.practicum.config.AppConfig;
import ru.yandex.practicum.crypto.Base64UrlCodec;
import ru.yandex.practicum.server.HmacServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

class ServerHMACTest {
    private HmacServer server;
    private HttpClient client;
    private int port;
    private static final String SECRET = "flmIR3VTzrcCUW8T4DvpMee1MlUxet37Cohm9NHSvkA=";

    @BeforeEach
    void setUp() throws IOException {
        port = 18080 + ThreadLocalRandom.current().nextInt(1000);
        AppConfig config = new AppConfig("HmacSHA256", Base64.getDecoder().decode(SECRET), port, 1024);
        server = new HmacServer(config);
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void signAndVerifySuccess() throws IOException, InterruptedException {
        String message = "hello";

        String signature = signMessage(message);
        assertNotNull(signature);

        boolean isValid = verifyMessage(message, signature);
        assertTrue(isValid);
    }

    @Test
    void invalidSignatureShouldFail() throws IOException, InterruptedException {
        String message = "hello";
        String signature = signMessage(message);

        byte[] signatureBytes = Base64UrlCodec.decode(signature);
        signatureBytes[0] ^= 0x01;
        String corruptedSignature = Base64UrlCodec.encode(signatureBytes);

        boolean isValid = verifyMessage(message, corruptedSignature);
        assertFalse(isValid);
    }

    @Test
    void modifiedMessageShouldFail() throws IOException, InterruptedException {
        String originalMessage = "hello";
        String signature = signMessage(originalMessage);

        boolean isValid = verifyMessage("hello!", signature);
        assertFalse(isValid);
    }

    @Test
    void invalidBase64UrlShouldReturn400() throws IOException, InterruptedException {
        String json = String.format("{\"msg\":\"hello\",\"signature\":\"%s\"}", "@@@");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/verify"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());

        JsonObject error = com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();
        assertEquals("invalid_signature_format", error.get("error").getAsString());
    }

    @Test
    void emptyMessageShouldReturn400() throws IOException, InterruptedException {
        String json = "{\"msg\":\"\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/sign"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
    }

    @Test
    void largeMessageShouldReturn413() throws IOException, InterruptedException {
        StringBuilder largeMessage = new StringBuilder();
        largeMessage.append("a".repeat(1500));

        String json = String.format("{\"msg\":\"%s\"}", largeMessage);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/sign"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(413, response.statusCode());
    }

    private String signMessage(String message) throws IOException, InterruptedException {
        String json = String.format("{\"msg\":\"%s\"}", message);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/sign"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        JsonObject result = com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();
        return result.get("signature").getAsString();
    }

    private boolean verifyMessage(String message, String signature) throws IOException, InterruptedException {
        String json = String.format("{\"msg\":\"%s\",\"signature\":\"%s\"}", message, signature);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/verify"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        JsonObject result = com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();
        return result.get("ok").getAsBoolean();
    }
}