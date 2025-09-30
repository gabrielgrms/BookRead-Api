package com.bookread.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    private final String SECRET_KEY = "your_secret_key_here"; // Use env variable in production

    public String generateToken(String username, String userLevel) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userLevel", userLevel)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // Add validation methods here (parse claims, check expiration, etc.)
}