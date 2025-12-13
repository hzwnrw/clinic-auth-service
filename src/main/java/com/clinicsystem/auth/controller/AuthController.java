package com.clinicsystem.auth.controller;

import com.clinicsystem.auth.dto.LoginRequest;
import com.clinicsystem.auth.dto.AuthResponse;
import com.clinicsystem.auth.dto.RegisterRequest;
import com.clinicsystem.auth.security.SecurityCipher;
import com.clinicsystem.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {


    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final long BLACKLIST_TTL_SECONDS = 86_400; // 1 day
    private final StringRedisTemplate redisTemplate;
    private final SecurityCipher securityCipher;
    private final AuthService authService;
    private final String env;

    public AuthController(SecurityCipher securityCipher, AuthService authService, @Value("${app.env:DEV}") String env, StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.securityCipher = securityCipher;
        this.authService = authService;
        this.env = env;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            // Assume authService.login returns an object with a token field
            AuthResponse authResponse = authService.login(request);
            if (authResponse == null || authResponse.getToken() == null) {
                logger.error("Auth service returned null response or token for request: {}", request);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: No token provided");
            }

            String jwtToken = authResponse.getToken();
            logger.debug("JWT from auth service: {}", jwtToken);

            if (!isJwtFormat(jwtToken)) {
                logger.error("Invalid JWT format from auth service: {}", jwtToken);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Invalid JWT format");
            }

            String encryptedToken = securityCipher.encrypt(jwtToken);
            if (encryptedToken == null) {
                logger.error("Failed to encrypt JWT: {}", jwtToken);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Encryption failed");
            }
            logger.debug("Encrypted token: {}", encryptedToken);

            boolean isProduction = env.equalsIgnoreCase("PROD");
            ResponseCookie jwtCookie = ResponseCookie.from("token", encryptedToken)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite(isProduction ? "Strict" : "Lax")
                    .path("/")
                    .maxAge(86400) // 1 day
                    .build();

            // Add cookie to response header
            response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

            return ResponseEntity.ok("Login successful");
        } catch (Exception e) {
            logger.error("Login failed for request: {}", request, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed: " + e.getMessage());
        }
    }

    private boolean isJwtFormat(String token) {
        return token != null && token.contains(".") && token.split("\\.").length == 3;
    }

//    @PostMapping("/register")
//    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
//        return ResponseEntity.ok(authService.register(request));
//    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. Expire the token cookie
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        // 2. Extract encrypted token from request cookies
        Optional<String> encryptedToken = Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(c -> "token".equals(c.getName()))
                        .map(Cookie::getValue)
                        .findFirst());

        if (encryptedToken.isEmpty()) {
            logger.warn("No token found to blacklist");
            return ResponseEntity.ok("Logged out successfully");
        }

        try {
            String token = securityCipher.decrypt(encryptedToken.get());
            if (token == null) {
                logger.error("Decryption failed for token");
                return ResponseEntity.ok("Logged out successfully");
            }

            // 3. Blacklist token in Redis with fixed 1-day TTL
            try {
                redisTemplate.opsForValue().set("blacklist:" + token, "true", Duration.ofSeconds(BLACKLIST_TTL_SECONDS));
                logger.info("Token blacklisted in Redis for {} seconds", BLACKLIST_TTL_SECONDS);
            } catch (Exception e) {
                logger.error("Error saving to Redis blacklist", e);
            }
            return ResponseEntity.ok("Logged out successfully");
        } catch (Exception e) {
            logger.error("Error decrypting token", e);
            return ResponseEntity.ok("Logged out successfully");
        }
    }

//    @GetMapping("/headers")
//    public Map<String, String> getHeaders(HttpServletRequest request) {
//        Map<String, String> headers = new HashMap<>();
//        headers.put("X-Forwarded-For", request.getHeader("X-Forwarded-For"));
//        headers.put("X-Real-IP", request.getHeader("X-Real-IP"));
//        headers.put("RemoteAddr", request.getRemoteAddr());
//        return headers;
//    }
}