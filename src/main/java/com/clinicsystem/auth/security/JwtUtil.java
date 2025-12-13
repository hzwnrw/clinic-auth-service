package com.clinicsystem.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${jwt.expiration}")
    private Long expiration;

    private final PrivateKey privateKey;

    public JwtUtil() {
        try {
            // Load private key from the classpath
            String privateKeyContent = getString();

            // Decode the Base64-encoded key
            System.out.println("Private Key Content: " + privateKeyContent);


            byte[]  privateKeyBytes = Base64.getDecoder().decode(privateKeyContent);


            // Create the PrivateKey object
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privateKey = keyFactory.generatePrivate(privateKeySpec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Failed to load private key", e);
        }
    }

    private static String getString() throws IOException {
        InputStream privateKeyStream = new ClassPathResource("private-key.pem").getInputStream();
        String privateKeyContent = new String(privateKeyStream.readAllBytes(), StandardCharsets.UTF_8);


        // Remove PEM headers/footers and whitespace
        privateKeyContent = privateKeyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", ""); // Remove all whitespace and newlines
        return privateKeyContent;
    }

    public String generateToken(String username, Collection<? extends GrantedAuthority> roles) {
        List<String> roleNames = roles.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(username) // Set the subject (username)
                .claim("roles", roleNames) // Add roles as a custom claim
                .setIssuedAt(new Date()) // Set the issue time
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // Set the expiration time
                .signWith(privateKey, SignatureAlgorithm.RS256) // Sign the token with RSA private key
                .compact();
    }
}