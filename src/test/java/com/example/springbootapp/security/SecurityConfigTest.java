package com.example.springbootapp.security;

import com.example.springbootapp.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SecurityConfigTest {
    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    public void passwordEncoderBcryptValid() {
        String rawPassword = "mySecretPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        assertFalse(passwordEncoder.matches("wrongPassword", encodedPassword));
    }

    @Test
    public void passwordEncoderEncodes() {
        String password1 = "password1";
        String encoded1 = passwordEncoder.encode(password1);
        String encoded2 = passwordEncoder.encode(password1);
        
        // Two encoding of same password should produce different results (salt)
        assertNotEquals(encoded1, encoded2);
        assertTrue(passwordEncoder.matches(password1, encoded1));
        assertTrue(passwordEncoder.matches(password1, encoded2));
    }
}
