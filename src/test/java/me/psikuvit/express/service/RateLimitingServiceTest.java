package me.psikuvit.express.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RateLimitingService Tests")
class RateLimitingServiceTest {

    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        rateLimitingService = new RateLimitingService();
    }

    @Test
    @DisplayName("Should allow requests within rate limit for regular endpoints")
    void testAllowRequestsWithinLimit() {
        String clientIp = "192.168.1.1";

        // Should allow up to 100 requests per minute
        for (int i = 0; i < 100; i++) {
            assertTrue(rateLimitingService.tryConsume(clientIp, false),
                    "Request " + i + " should be allowed within rate limit");
        }
    }

    @Test
    @DisplayName("Should reject requests exceeding rate limit for regular endpoints")
    void testRejectRequestsExceedingLimit() {
        String clientIp = "192.168.1.2";

        // Consume all tokens
        for (int i = 0; i < 100; i++) {
            rateLimitingService.tryConsume(clientIp, false);
        }

        // Next request should be rejected
        assertFalse(rateLimitingService.tryConsume(clientIp, false),
                "Request exceeding rate limit should be rejected");
    }

    @Test
    @DisplayName("Should enforce stricter limits for auth endpoints")
    void testStricterLimitsForAuthEndpoints() {
        String clientIp = "192.168.1.3";

        // Auth endpoints have limit of 5 requests per minute
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimitingService.tryConsume(clientIp, true),
                    "Auth request " + i + " should be allowed");
        }

        // 6th request should be rejected
        assertFalse(rateLimitingService.tryConsume(clientIp, true),
                "Auth request exceeding limit should be rejected");
    }

    @Test
    @DisplayName("Should track available tokens correctly")
    void testAvailableTokensTracking() {
        String clientIp = "192.168.1.4";

        long initialTokens = rateLimitingService.getAvailableTokens(clientIp, false);
        assertEquals(100, initialTokens, "Should have 100 tokens initially for regular endpoints");

        rateLimitingService.tryConsume(clientIp, false);
        long tokensAfterOne = rateLimitingService.getAvailableTokens(clientIp, false);
        assertEquals(initialTokens - 1, tokensAfterOne, "Should have 99 tokens after consuming one");
    }

    @Test
    @DisplayName("Should maintain separate buckets for different IP addresses")
    void testSeparateBucketsForDifferentIPs() {
        String ip1 = "192.168.1.5";
        String ip2 = "192.168.1.6";

        // Consume tokens for IP1
        for (int i = 0; i < 50; i++) {
            rateLimitingService.tryConsume(ip1, false);
        }

        // IP2 should still have full bucket
        long ip2Tokens = rateLimitingService.getAvailableTokens(ip2, false);
        assertEquals(100, ip2Tokens, "Different IP should have independent bucket");
    }

    @Test
    @DisplayName("Should differentiate between auth and regular endpoints per IP")
    void testDifferentiateBetweenEndpointTypes() {
        String clientIp = "192.168.1.7";

        // Auth endpoint bucket should be separate from regular endpoint bucket
        rateLimitingService.tryConsume(clientIp, true);
        long authTokens = rateLimitingService.getAvailableTokens(clientIp, true);
        assertEquals(4, authTokens, "Auth endpoint should have 4 tokens remaining");

        long regularTokens = rateLimitingService.getAvailableTokens(clientIp, false);
        assertEquals(100, regularTokens, "Regular endpoint should have independent bucket");
    }
}

