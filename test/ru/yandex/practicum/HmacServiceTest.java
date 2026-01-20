package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.common.exceptions.CryptoException;
import ru.yandex.practicum.crypto.HmacService;

import static org.junit.jupiter.api.Assertions.*;

class HmacServiceTest {
    private HmacService hmacService;

    @BeforeEach
    void setUp() {
        byte[] testSecret = "test-secret-key-123".getBytes();
        hmacService = new HmacService(testSecret, "SHA256");
    }

    @Test
    void sign_ReturnsValidBase64UrlSignature() throws CryptoException {
        String message = "test message";

        String signature = hmacService.sign(message);

        assertNotNull(signature);
        assertTrue(signature.matches("[A-Za-z0-9_-]+"));
        assertFalse(signature.contains("="));
        assertFalse(signature.contains("+"));
        assertFalse(signature.contains("/"));
    }

    @Test
    void verify_ReturnsTrue_ForValidSignature() throws CryptoException {
        String message = "hello world";
        String signature = hmacService.sign(message);

        boolean isValid = hmacService.verify(message, signature);

        assertTrue(isValid);
    }

    @Test
    void verify_ReturnsFalse_ForTamperedSignature() throws CryptoException {
        String message = "original message";
        String originalSignature = hmacService.sign(message);
        String tamperedSignature = originalSignature.substring(0, originalSignature.length() - 2) + "XX";

        boolean isValid = hmacService.verify(message, tamperedSignature);

        assertFalse(isValid);
    }

    @Test
    void verify_ReturnsFalse_ForDifferentMessage() throws CryptoException {
        String originalMessage = "first message";
        String signature = hmacService.sign(originalMessage);

        boolean isValid = hmacService.verify("different message", signature);

        assertFalse(isValid);
    }

    @Test
    void sign_ProducesConsistentSignatures_ForSameInput() throws CryptoException {
        String message = "consistent test";

        String signature1 = hmacService.sign(message);
        String signature2 = hmacService.sign(message);

        assertEquals(signature1, signature2);
    }

    @Test
    void sign_HandlesEmptyString() throws CryptoException {
        String signature = hmacService.sign("");

        assertNotNull(signature);
        assertTrue(signature.matches("[A-Za-z0-9_-]+"));
    }

    @Test
    void verify_ReturnsFalse_ForInvalidBase64Url() throws CryptoException {
        String message = "test";
        String invalidSignature = "@@@invalid@@@";

        boolean isValid = hmacService.verify(message, invalidSignature);

        assertFalse(isValid);
    }

    @Test
    void sign_deterministicSignatures() throws CryptoException {
        HmacService service1 = new HmacService("test-secret".getBytes(), "HmacSHA256");
        HmacService service2 = new HmacService("test-secret".getBytes(), "HmacSHA256");

        String message = "hello world";
        String signature1 = service1.sign(message);
        String signature2 = service2.sign(message);

        assertEquals(signature1, signature2);

        for (int i = 0; i < 10; i++) {
            assertEquals(signature1, service1.sign(message));
        }
    }
}