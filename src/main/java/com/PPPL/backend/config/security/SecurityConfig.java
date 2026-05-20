package com.PPPL.backend.config.security;

import com.PPPL.backend.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    /**
     * CORS Configuration with proper security
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allowed origins - dynamically set based on environment
        configuration.setAllowedOrigins(Arrays.asList(
            frontendUrl,
            "http://localhost:4200" // For local development
        ));
        
        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Allowed headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With",
            "Cache-Control"
        ));
        
        // Exposed headers (visible to frontend)
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Retry-After",
            "X-Total-Count"
        ));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Preflight cache duration (1 hour)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (using JWT)
            .csrf(csrf -> csrf.disable())
            
            // Enable CORS with proper configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Stateless session (JWT-based)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Authorization rules
            .authorizeHttpRequests(auth -> auth

                // Public endpoints - no authentication required
                .requestMatchers(
                    "/",
                    "/health",
                    "/actuator/health",
                    "/api/auth/**",
                    "/api/public/**",
                    "/api/system/keepalive/**",
                    "/uploads/**",
                    "/ws/**",
                    "/api/ws/**"
                ).permitAll()

                // Admin endpoints - SUPER_ADMIN or MANAGER
                .requestMatchers("/api/admin/**")
                    .hasAnyRole("SUPER_ADMIN", "MANAGER")

                // Manager endpoints
                .requestMatchers("/api/manager/**")
                    .hasRole("SUPER_ADMIN")

                // Karyawan management - SUPER_ADMIN only
                .requestMatchers("/api/karyawan/**")
                    .hasRole("SUPER_ADMIN")

                // Klien management - SUPER_ADMIN or MANAGER
                .requestMatchers("/api/klien/**")
                    .hasAnyRole("SUPER_ADMIN", "MANAGER")

                // Layanan management - SUPER_ADMIN only
                .requestMatchers("/api/layanan/**")
                    .hasRole("SUPER_ADMIN")

                // Request layanan - SUPER_ADMIN or MANAGER
                .requestMatchers("/api/request-layanan/**")
                    .hasAnyRole("SUPER_ADMIN", "MANAGER")

                // Rekap - SUPER_ADMIN or MANAGER
                .requestMatchers("/api/rekap/**")
                    .hasAnyRole("SUPER_ADMIN", "MANAGER")

                // All other requests require authentication
                .anyRequest().authenticated()
            );

        // Add JWT filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}