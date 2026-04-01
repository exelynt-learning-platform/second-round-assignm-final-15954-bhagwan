package com.example.springbootapp.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private final Key key;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret:}") String secret,
                   @Value("${jwt.expiration-ms:3600000}") long expirationMs) {
        // Enforce at least 256 bits (32 bytes) for HMAC-SHA256
        // Fail fast if JWT secret is missing or insufficient
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT secret is required and must be configured via 'jwt.secret' environment variable. " +
                    "Provide a minimum 32-byte secure secret.");
        }
        
        if (secret.getBytes().length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret is too short. Minimum 32 bytes required, but got " + secret.getBytes().length + " bytes. " +
                    "Generate a secure 32+ byte secret and set via 'jwt.secret' environment variable.");
        }
        
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
        logger.info("JWT utility initialized with {} byte secret", secret.getBytes().length);
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
