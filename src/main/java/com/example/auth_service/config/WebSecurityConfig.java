package com.example.auth_service.config;




import com.example.auth_service.filters.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;


@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class WebSecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;

    @Value("${api.prefix}")
    private String apiPrefix;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // ✅ Disable CSRF
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // ✅ Stateless session
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // ✅ Enable CORS
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class) // ✅ Add JWT filter
                .authorizeHttpRequests(requests -> requests
                                // ✅ Public Endpoints
                                .requestMatchers(
                                        String.format("%s/auth/register", apiPrefix),
                                        String.format("%s/auth/otp/send", apiPrefix),
                                        String.format("%s/auth/reset-password", apiPrefix),
                                        String.format("%s/auth/login-google", apiPrefix),
                                        String.format("%s/auth/otp/verify", apiPrefix),
                                        String.format("%s/auth/login", apiPrefix)
//                                        String.format("%s/users", apiPrefix),
//                                        String.format("%s/users/add", apiPrefix)

                                ).permitAll()

                                // ✅ Public Endpoint for users with dynamic ID (e.g., /users/{id}/avatar)
                                .requestMatchers(
                                        req -> req.getServletPath().contains(
                                                String.format("%s/users/", apiPrefix)
                                        ) // Kiểm tra chỉ URL
                                ).permitAll()

                                // ✅ Orders - Uncomment and modify this part if you have role-based access to orders
//                        .requestMatchers(PUT, String.format("%s/orders/**", apiPrefix)).hasRole(Role.ADMIN)
//                        .requestMatchers(GET, String.format("%s/orders/**", apiPrefix)).hasAnyRole(Role.ADMIN, Role.USER)
//                        .requestMatchers(POST, String.format("%s/orders/**", apiPrefix)).hasRole(Role.USER)
//                        .requestMatchers(DELETE, String.format("%s/orders/**", apiPrefix)).hasRole(Role.ADMIN)

                                .anyRequest().authenticated()  // ✅ All other requests require authentication
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "x-auth-token"));
        config.setExposedHeaders(List.of("Authorization", "x-auth-token"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
