// ✅ STEP 1: Update SecurityConfig.java
// src/main/java/com/nearfix/nearfix/config/SecurityConfig.java

package com.nearfix.nearfix.config;

import com.nearfix.nearfix.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configure(http))
                .authorizeHttpRequests(auth -> auth
                        // ========================================
                        // ✅ PUBLIC ENDPOINTS (No Authentication)
                        // ========================================

                        // Auth endpoints
                        .requestMatchers("/auth/otp/**").permitAll()

                        // Service browsing (READ-ONLY)
                        .requestMatchers(HttpMethod.GET, "/api/services/**").permitAll()

                        // Provider search & profiles (READ-ONLY)
                        .requestMatchers(HttpMethod.POST, "/api/search/providers").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/search/providers/**").permitAll()

                        // View provider services (READ-ONLY)
                        .requestMatchers(HttpMethod.GET, "/api/provider/services/**").permitAll()

                        // Read reviews (READ-ONLY)
                        .requestMatchers(HttpMethod.GET, "/api/reviews/provider/**").permitAll()

                        // File access (images, documents)
                        .requestMatchers("/api/files/**").permitAll()

                        // ========================================
                        // 🔐 PROTECTED ENDPOINTS (Require Auth)
                        // ========================================

                        // Bookings - PROVIDER endpoints (must come before the broader
                        // "/api/bookings/**" customer rule below, since Spring Security
                        // uses first-match-wins for authorizeHttpRequests)
                        .requestMatchers(HttpMethod.GET, "/api/bookings/provider").hasRole("PROVIDER")
                        .requestMatchers(HttpMethod.PUT, "/api/bookings/*/accept").hasRole("PROVIDER")
                        .requestMatchers(HttpMethod.PUT, "/api/bookings/*/reject").hasRole("PROVIDER")
                        .requestMatchers(HttpMethod.PUT, "/api/bookings/*/complete").hasRole("PROVIDER")

                        // Bookings - shared CUSTOMER/PROVIDER endpoints (cancel and get-by-id
                        // are used by both roles; service layer enforces per-booking access)
                        .requestMatchers(HttpMethod.PUT, "/api/bookings/*/cancel").hasAnyRole("CUSTOMER", "PROVIDER")
                        .requestMatchers(HttpMethod.GET, "/api/bookings/{id:[0-9]+}").hasAnyRole("CUSTOMER", "PROVIDER")

                        // Bookings - CUSTOMER only (everything else, e.g. create, list own, attachments)
                        .requestMatchers("/api/bookings/**").hasRole("CUSTOMER")

                        // Payments - CUSTOMER only
                        .requestMatchers("/api/payments/**").hasRole("CUSTOMER")

                        // Write reviews - CUSTOMER only
                        .requestMatchers(HttpMethod.POST, "/api/reviews").hasRole("CUSTOMER")

                        // Provider dashboard & management
                        .requestMatchers("/api/provider/profile/**").hasRole("PROVIDER")
                        .requestMatchers(HttpMethod.POST, "/api/provider/services").hasRole("PROVIDER")
                        .requestMatchers(HttpMethod.PUT, "/api/provider/services/**").hasRole("PROVIDER")
                        .requestMatchers(HttpMethod.DELETE, "/api/provider/services/**").hasRole("PROVIDER")

                        // Admin panel
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}