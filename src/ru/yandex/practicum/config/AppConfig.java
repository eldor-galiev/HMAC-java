package ru.yandex.practicum.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class AppConfig {
    public AppConfig(String hmacAlg, byte[] secret, int listenPort, int maxMsgSizeBytes) {
        this.hmacAlg = hmacAlg;
        this.secret = secret;
        this.listenPort = listenPort;
        this.maxMsgSizeBytes = maxMsgSizeBytes;
    }

    private String hmacAlg;
    private byte[] secret;
    private int listenPort;
    private int maxMsgSizeBytes;

    private static final Gson GSON = new Gson();

    private AppConfig() {

    }

    public static AppConfig load() throws IOException {
        return load(Paths.get("config.json"));
    }

    public static AppConfig load(Path configPath) throws IOException {
        if (!Files.exists(configPath)) {
            throw new IOException("Config file not found: " + configPath);
        }

        try {
            String content = Files.readString(configPath);
            JsonObject json = GSON.fromJson(content, JsonObject.class);

            AppConfig config = new AppConfig();
            config.hmacAlg = getString(json, "hmacAlg", "HmacSHA256");
            config.listenPort = getInt(json, "listenPort", 8080);
            config.maxMsgSizeBytes = getInt(json, "maxMsgSizeBytes", 1048576);

            String secretStr = getString(json, "secret", null);
            if (secretStr == null || secretStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Secret must be specified in config.json");
            }

            try {
                config.secret = Base64.getDecoder().decode(secretStr.trim());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid base64 secret", e);
            }

            validateConfig(config);
            return config;

        } catch (JsonSyntaxException e) {
            throw new IOException("Invalid JSON in config file", e);
        }
    }

    private static String getString(JsonObject json, String key, String defaultValue) {
        return json.has(key) ? json.get(key).getAsString() : defaultValue;
    }

    private static int getInt(JsonObject json, String key, int defaultValue) {
        return json.has(key) ? json.get(key).getAsInt() : defaultValue;
    }

    private static void validateConfig(AppConfig config) throws IOException {
        if (config.secret.length < 32) {
            throw new IOException("Secret must be at least 32 bytes (256 bits)");
        }
        if (config.listenPort < 1 || config.listenPort > 65535) {
            throw new IOException("Port must be between 1 and 65535");
        }
        if (config.maxMsgSizeBytes < 1) {
            throw new IOException("maxMsgSizeBytes must be positive");
        }
    }

    public String getHmacAlg() {
        return hmacAlg;
    }

    public byte[] getSecret() {
        return secret.clone();
    }

    public int getListenPort() {
        return listenPort;
    }

    public int getMaxMsgSizeBytes() {
        return maxMsgSizeBytes;
    }
}