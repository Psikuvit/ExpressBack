package me.psikuvit.express.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing API rate limits using Bucket4j token bucket algorithm.
 * Implements per-IP rate limiting to prevent abuse.
 */
@Service
public class RateLimitingService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Resolves a bucket for the given key (typically an IP address).
     * Creates a new bucket if one doesn't exist for this key.
     * <p>
     * Rate limits:
     * - Auth endpoints: 5 requests per minute (prevents brute force)
     * - API endpoints: 100 requests per minute (normal usage)
     *
     * @param key The identifier (usually IP address)
     * @param isAuthEndpoint Whether this is an authentication endpoint
     * @return Bucket for rate limiting
     */
    public Bucket resolveBucket(String key, boolean isAuthEndpoint) {
        return cache.computeIfAbsent(key, k -> createNewBucket(isAuthEndpoint));
    }

    /**
     * Creates a new rate limit bucket with appropriate limits.
     *
     * @param isAuthEndpoint If true, applies stricter limits for auth endpoints
     * @return A new Bucket instance
     */
    private Bucket createNewBucket(boolean isAuthEndpoint) {
        Bandwidth limit;

        if (isAuthEndpoint) {
            // Stricter limit for authentication endpoints (5 requests per minute)
            limit = Bandwidth.builder()
                    .capacity(5)
                    .refillIntervally(5, Duration.ofMinutes(1))
                    .build();
        } else {
            // Standard limit for other API endpoints (100 requests per minute)
            limit = Bandwidth.builder()
                    .capacity(100)
                    .refillIntervally(100, Duration.ofMinutes(1))
                    .build();
        }

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Checks if the request should be allowed based on rate limits.
     *
     * @param key The identifier (usually IP address)
     * @param isAuthEndpoint Whether this is an authentication endpoint
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean tryConsume(String key, boolean isAuthEndpoint) {
        Bucket bucket = resolveBucket(key, isAuthEndpoint);
        return bucket.tryConsume(1);
    }

    /**
     * Gets the number of available tokens for a given key.
     * Useful for debugging or providing rate limit information to clients.
     *
     * @param key The identifier (usually IP address)
     * @param isAuthEndpoint Whether this is an authentication endpoint
     * @return Number of available tokens
     */
    public long getAvailableTokens(String key, boolean isAuthEndpoint) {
        Bucket bucket = resolveBucket(key, isAuthEndpoint);
        return bucket.getAvailableTokens();
    }
}

