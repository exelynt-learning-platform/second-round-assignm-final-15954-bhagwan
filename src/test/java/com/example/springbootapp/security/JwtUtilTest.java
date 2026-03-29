package com.example.springbootapp.security;

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
}
