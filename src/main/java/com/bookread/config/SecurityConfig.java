package com.bookread.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/users", "/users/**").permitAll() // Allow POST /users and GET /users
                        .anyRequest().authenticated() // Require authentication for other endpoints
                )
                .csrf(csrf -> csrf.disable()); // Disable CSRF for simplicity (enable for production with proper config)
        return http.build();
    }
}