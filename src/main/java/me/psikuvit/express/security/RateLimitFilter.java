package me.psikuvit.express.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.psikuvit.express.service.RateLimitingService;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that applies rate limiting to all incoming HTTP requests.
 * Uses the RateLimitingService to enforce rate limits based on IP address.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;

    public RateLimitFilter(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String key = getClientIP(request);
        String requestURI = request.getRequestURI();

        // Determine if this is an auth endpoint (stricter limits)
        boolean isAuthEndpoint = requestURI.startsWith("/api/auth/");

        // Try to consume a token from the bucket
        if (rateLimitingService.tryConsume(key, isAuthEndpoint)) {
            // Add rate limit headers for client information
            long availableTokens = rateLimitingService.getAvailableTokens(key, isAuthEndpoint);
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(availableTokens));

            // Allow the request to proceed
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\": \"Too many requests\", " +
                "\"message\": \"Rate limit exceeded. Please try again later.\", " +
                "\"status\": 429}"
            );
        }
    }

    /**
     * Extracts the client IP address from the request.
     * Checks common proxy headers first, then falls back to remote address.
     *
     * @param request The HTTP request
     * @return The client's IP address
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty() && !"unknown".equalsIgnoreCase(xfHeader)) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xfHeader.split(",")[0].trim();
        }

        String xrHeader = request.getHeader("X-Real-IP");
        if (xrHeader != null && !xrHeader.isEmpty() && !"unknown".equalsIgnoreCase(xrHeader)) {
            return xrHeader;
        }

        return request.getRemoteAddr();
    }
}


