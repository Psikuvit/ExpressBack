package me.psikuvit.express.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AppAccessFilter extends OncePerRequestFilter {

    private final Set<String> allowedOrigins;
    private final String appToken;
    private final boolean enforceAppToken;

    public AppAccessFilter(
            @Value("${security.allowed.origins:}") String allowedOriginsCsv,
            @Value("${security.app.token:}") String appToken,
            @Value("${security.enforce.app-token:true}") boolean enforceAppToken
    ) {
        this.allowedOrigins = parseAllowedOrigins(allowedOriginsCsv);
        this.appToken = appToken;
        this.enforceAppToken = enforceAppToken;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // First, enforce app token if enabled (this applies to all requests)
        if (enforceAppToken) {
            String headerToken = request.getHeader("X-App-Token");
            if (appToken == null || appToken.isBlank() || !appToken.equals(headerToken)) {
                reject(response, "Missing or invalid app token. Provide valid X-App-Token header.");
                return;
            }
        }

        String origin = request.getHeader("Origin");

        // If allowed origins contains "*", allow all origins (all clients with different IPs)
        if (!allowedOrigins.isEmpty() && allowedOrigins.contains("*")) {
            filterChain.doFilter(request, response);
            return;
        }

        // If specific origins are configured, validate them
        if (origin != null && !origin.isBlank()) {
            if (!allowedOrigins.isEmpty() && !allowedOrigins.contains(origin)) {
                reject(response, "Origin is not allowed.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private Set<String> parseAllowedOrigins(String allowedOriginsCsv) {
        if (allowedOriginsCsv == null || allowedOriginsCsv.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(allowedOriginsCsv.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toSet());
    }

    private void reject(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"error\": \"Forbidden\", \"message\": \"" + message + "\", \"status\": 403}"
        );
    }
}

