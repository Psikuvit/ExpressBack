package me.psikuvit.express.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    private final AuthTokenFilter authTokenFilter;

    private final RateLimitFilter rateLimitFilter;
    private final AppAccessFilter appAccessFilter;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService, AuthTokenFilter authTokenFilter, RateLimitFilter rateLimitFilter, AppAccessFilter appAccessFilter) {
        this.userDetailsService = userDetailsService;
        this.authTokenFilter = authTokenFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.appAccessFilter = appAccessFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(org.springframework.security.config.Customizer.withDefaults())
                .headers(headers -> headers
                        .contentTypeOptions(org.springframework.security.config.Customizer.withDefaults())
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .xssProtection(org.springframework.security.config.Customizer.withDefaults())
                        .cacheControl(org.springframework.security.config.Customizer.withDefaults())
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        // Order: AppAccess -> RateLimit -> JWT Auth
        http.addFilterAfter(appAccessFilter, SecurityContextHolderFilter.class);
        http.addFilterAfter(rateLimitFilter, AppAccessFilter.class);
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${security.allowed.origins:}") String allowedOriginsCsv
    ) {
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();

        // Handle wildcard to allow all origins
        if ("*".equals(allowedOriginsCsv)) {
            config.setAllowedOriginPatterns(List.of("*"));
            config.setAllowCredentials(false);
        } else {
            config.setAllowedOrigins(parseAllowedOrigins(allowedOriginsCsv));
        }

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("X-Rate-Limit-Remaining", "Authorization"));

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private List<String> parseAllowedOrigins(String allowedOriginsCsv) {
        if (allowedOriginsCsv == null || allowedOriginsCsv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(allowedOriginsCsv.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }
}
