package ru.yandex.practicum.crypto;

import java.util.Base64;

public class Base64UrlCodec {
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    public static String encode(byte[] data) {
        return ENCODER.encodeToString(data);
    }

    public static byte[] decode(String base64url) {
        if (base64url == null || base64url.trim().isEmpty()) {
            throw new IllegalArgumentException("Input string is null or empty");
        }

        if (!base64url.matches("^[A-Za-z0-9_-]*$")) {
            throw new IllegalArgumentException("Invalid base64url characters");
        }

        try {
            return DECODER.decode(base64url);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid base64url format", e);
        }
    }

    public static boolean isValidBase64Url(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        return input.matches("^[A-Za-z0-9_-]*$");
    }
}
