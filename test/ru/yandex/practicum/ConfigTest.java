package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.yandex.practicum.config.AppConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {
    @TempDir
    Path tempDir;

    @Test
    void load_ReturnsConfig_WithValidValues() throws IOException {
        String configContent = """
            {
                "hmacAlg": "SHA256",
                "secret": "UNja/k6B+B4kP2oz0WDg6EJ9C+rg8mWjjgOaHVnciyc=",
                "listenPort": 8080,
                "maxMsgSizeBytes": 1048576
            }
            """;
        Path configFile = tempDir.resolve("config.json");
        Files.write(configFile, configContent.getBytes());

        AppConfig config = AppConfig.load(configFile);

        assertEquals("SHA256", config.getHmacAlg());
        assertArrayEquals(Base64.getDecoder().decode("UNja/k6B+B4kP2oz0WDg6EJ9C+rg8mWjjgOaHVnciyc="), config.getSecret());
        assertEquals(8080, config.getListenPort());
        assertEquals(1048576, config.getMaxMsgSizeBytes());
    }

    @Test
    void load_ThrowsException_WhenFileNotFound() {
        assertThrows(IOException.class, () -> AppConfig.load(Path.of("nonexistent.json")));
    }

    @Test
    void load_ThrowsException_WhenInvalidJson() throws IOException {
        Path configFile = tempDir.resolve("config.json");
        Files.write(configFile, "invalid json".getBytes());

        assertThrows(IOException.class, () -> AppConfig.load(configFile));
    }

    @Test
    void load_ThrowsException_WhenMissingSecret() throws IOException {
        String configContent = """
            {
                "hmacAlg": "SHA256",
                "listenPort": 8080
            }
            """;
        Path configFile = tempDir.resolve("config.json");
        Files.write(configFile, configContent.getBytes());

        assertThrows(IllegalArgumentException.class, () -> AppConfig.load(configFile));
    }

    @Test
    void load_ThrowsException_WhenInvalidBase64Secret() throws IOException {
        String configContent = """
            {
                "hmacAlg": "SHA256",
                "secret": "not-base64!",
                "listenPort": 8080
            }
            """;
        Path configFile = tempDir.resolve("config.json");
        Files.write(configFile, configContent.getBytes());

        assertThrows(IllegalArgumentException.class, () -> AppConfig.load(configFile));
    }
}