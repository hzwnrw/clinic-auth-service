package com.clinicsystem.auth.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

@Component
public class SecurityCipher {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityCipher.class);

    private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BIT = 128;
    private static final int KEY_SIZE = 256;
    private static final int ITERATIONS = 65536;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${app.encryption.secret}")
    private String secretKey;

    @Value("${app.encryption.salt}")
    private String salt;

    public String encrypt(String plainText) {
        if (plainText == null) {
            LOGGER.error("Cannot encrypt null plaintext");
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);

            SecretKey secretKey = getSecretKey();

            Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedBytes.length);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedBytes);

            String encrypted = Base64.getEncoder().encodeToString(byteBuffer.array());
            LOGGER.debug("Encrypted text: {}", encrypted);
            return encrypted;

        } catch (Exception e) {
            LOGGER.error("Encryption failed for plaintext: {}", plainText, e);
            return null;
        }
    }

    public String decrypt(String encryptedBase64) {
        if (encryptedBase64 == null) {
            LOGGER.error("Cannot decrypt null encrypted text");
            return null;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedBase64);
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[IV_LENGTH];
            byteBuffer.get(iv);

            byte[] cipherBytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherBytes);

            SecretKey secretKey = getSecretKey();

            Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] decryptedBytes = cipher.doFinal(cipherBytes);
            String decrypted = new String(decryptedBytes, StandardCharsets.UTF_8);
            LOGGER.debug("Decrypted text: {}", decrypted);
            return decrypted;

        } catch (Exception e) {
            LOGGER.error("Decryption failed for encrypted text: {}", encryptedBase64, e);
            return null;
        }
    }

    private SecretKey getSecretKey() throws Exception {
        if (secretKey == null || salt == null) {
            throw new IllegalStateException("Secret key or salt is not configured");
        }
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(StandardCharsets.UTF_8), ITERATIONS, KEY_SIZE);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }
}