package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.crypto.Base64UrlCodec;

import static org.junit.jupiter.api.Assertions.*;

class CodecTest {

    @Test
    void encodeToBase64Url_ReturnsValidString_WithoutPadding() {
        byte[] data = "test data".getBytes();

        String encoded = Base64UrlCodec.encode(data);

        assertNotNull(encoded);
        assertFalse(encoded.contains("="));
        assertFalse(encoded.contains("+"));
        assertFalse(encoded.contains("/"));
        assertTrue(encoded.matches("[A-Za-z0-9_-]+"));
    }

    @Test
    void isBase64Url_ReturnsTrue_ForValidString() {
        String valid = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";

        boolean result = Base64UrlCodec.isValidBase64Url(valid);

        assertTrue(result);
    }

    @Test
    void isBase64Url_ReturnsFalse_ForInvalidString() {
        assertFalse(Base64UrlCodec.isValidBase64Url("invalid+plus"));
        assertFalse(Base64UrlCodec.isValidBase64Url("invalid/slash"));
        assertFalse(Base64UrlCodec.isValidBase64Url("invalid=equals"));
        assertFalse(Base64UrlCodec.isValidBase64Url("invalid space"));
    }
}