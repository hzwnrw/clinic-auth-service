package com.clinicsystem.auth.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    // Store rate limit buckets per IP address
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // Create or get a bucket for a given IP address
    private Bucket resolveBucket(String clientIp) {
        return buckets.computeIfAbsent(clientIp, k -> {
            // Define rate limit: 10 requests per minute
            Bandwidth limit = Bandwidth.builder()
                    .capacity(10) // Maximum 10 tokens in the bucket
                    .refillGreedy(10, Duration.ofMinutes(1)) // Refill 10 tokens every minute
                    .build();
            return Bucket.builder().addLimit(limit).build();
        });
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Only apply rate limiting to /api/auth/** endpoints
        if (!request.getRequestURI().startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp != null && !clientIp.isEmpty()) {
            clientIp = clientIp.split(",")[0].trim();
            logger.info("Client IP (from X-Forwarded-For): {}", clientIp);
        } else {
            clientIp = request.getRemoteAddr();
            logger.info("Client IP (from RemoteAddr): {}", clientIp);
        }

        // Get or create the bucket for this IP
        Bucket bucket = resolveBucket(clientIp);

        logger.info("Request URI: {}", request.getRequestURI());
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            logger.warn("Rate limit exceeded for IP: {}, URI: {}", clientIp, request.getRequestURI());
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"Too many requests. Please try again later.\"}");
            response.getWriter().flush();
        }
    }
}