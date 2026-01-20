package ru.yandex.practicum.crypto;

import ru.yandex.practicum.common.exceptions.CryptoException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HmacService {
    private final byte[] secret;
    private final String algorithm;

    public HmacService(byte[] secret, String algorithm) {
        this.secret = secret.clone();
        this.algorithm = algorithm;
    }

    public String sign(String message) throws CryptoException {
        try {
            byte[] messageBytes = message.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(secret, algorithm));
            byte[] signature = mac.doFinal(messageBytes);
            return Base64UrlCodec.encode(signature);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("HMAC algorithm not available", e);
        } catch (InvalidKeyException e) {
            throw new CryptoException("Invalid secret key", e);
        }
    }

    public boolean verify(String message, String signature) throws CryptoException {
        try {
            String computedSignature = sign(message);
            return MessageDigest.isEqual(
                    Base64UrlCodec.decode(signature),
                    Base64UrlCodec.decode(computedSignature)
            );
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
