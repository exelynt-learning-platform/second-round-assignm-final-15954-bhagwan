package com.example.springbootapp.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {
    @Test
    public void generateAndValidateToken() {
        String secret = "01234567890123456789012345678901"; // 32 bytes
        long exp = 1000 * 60 * 60;
        JwtUtil jwt = new JwtUtil(secret, exp);
        String token = jwt.generateToken("alice");
        assertNotNull(token);
        assertTrue(jwt.validateToken(token));
        assertEquals("alice", jwt.getUsernameFromToken(token));
    }

    @Test
    public void generateTokenWithDifferentUsernames() {
        String secret = "01234567890123456789012345678901";
        JwtUtil jwt = new JwtUtil(secret, 3600000);
        
        String token1 = jwt.generateToken("user1");
        String token2 = jwt.generateToken("user2");
        
        assertNotEquals(token1, token2);
        assertEquals("user1", jwt.getUsernameFromToken(token1));
        assertEquals("user2", jwt.getUsernameFromToken(token2));
    }

    @Test
    public void invalidTokenThrowsException() {
        String secret = "01234567890123456789012345678901";
        JwtUtil jwt = new JwtUtil(secret, 3600000);
        
        assertFalse(jwt.validateToken("invalid.token.here"));
    }

    @Test
    public void getUsernameFromInvalidTokenThrowsException() {
        String secret = "01234567890123456789012345678901";
        JwtUtil jwt = new JwtUtil(secret, 3600000);
        
        assertThrows(Exception.class, () -> jwt.getUsernameFromToken("invalid.token.here"));
    }

    @Test
    public void tokenValidationFailsWithWrongSecret() {
        String secret1 = "01234567890123456789012345678901";
        String secret2 = "11111111111111111111111111111111";
        
        JwtUtil jwt1 = new JwtUtil(secret1, 3600000);
        JwtUtil jwt2 = new JwtUtil(secret2, 3600000);
        
        String token = jwt1.generateToken("testuser");
        
        assertFalse(jwt2.validateToken(token));
    }

    @Test
    public void tokenExpiresCorrectly() {
        String secret = "01234567890123456789012345678901";
        long expMs = 1; // 1ms expiration
        JwtUtil jwt = new JwtUtil(secret, expMs);
        
        String token = jwt.generateToken("user");
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertFalse(jwt.validateToken(token));
    }

    @Test
    public void generateTokenWithShortSecretGeneratesRandomKey() {
        String shortSecret = "short";
        JwtUtil jwt = new JwtUtil(shortSecret, 3600000);
        
        String token = jwt.generateToken("user");
        assertNotNull(token);
        assertEquals("user", jwt.getUsernameFromToken(token));
    }

    @Test
    public void generateTokenWithEmptySecretGeneratesRandomKey() {
        JwtUtil jwt = new JwtUtil("", 3600000);
        
        String token = jwt.generateToken("user");
        assertNotNull(token);
        assertEquals("user", jwt.getUsernameFromToken(token));
    }

    @Test
    public void generateTokenWithNullSecretGeneratesRandomKey() {
        JwtUtil jwt = new JwtUtil(null, 3600000);
        
        String token = jwt.generateToken("user");
        assertNotNull(token);
        assertEquals("user", jwt.getUsernameFromToken(token));
    }
}
