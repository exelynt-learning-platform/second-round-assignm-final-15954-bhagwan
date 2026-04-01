package com.example.springbootapp.security;

import com.example.springbootapp.Application;
import com.example.springbootapp.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Application.class, properties = {
    "jwt.secret=Test_JWT_Secret_That_Is_Long_Enough_For_256_Bits_ABCDEFGHIJKLMNOP",
    "stripe.api.key="
})
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

    @Test
    public void passwordEncoderWithSpecialCharacters() {
        String specialPassword = "P@ssw0rd!#$%^&*()";
        String encoded = passwordEncoder.encode(specialPassword);
        assertTrue(passwordEncoder.matches(specialPassword, encoded));
        assertFalse(passwordEncoder.matches("P@ssw0rd!#$%^&*(", encoded));
    }

    @Test
    public void passwordEncoderWithLongPassword() {
        String longPassword = "a".repeat(100);
        String encoded = passwordEncoder.encode(longPassword);
        assertTrue(passwordEncoder.matches(longPassword, encoded));
    }

    @Test
    public void passwordEncoderWithEmptyPassword() {
        String emptyPassword = "";
        String encoded = passwordEncoder.encode(emptyPassword);
        assertTrue(passwordEncoder.matches(emptyPassword, encoded));
    }

    @Test
    public void passwordEncoderWithWhitespace() {
        String passwordWithSpace = "my password 123";
        String encoded = passwordEncoder.encode(passwordWithSpace);
        assertTrue(passwordEncoder.matches(passwordWithSpace, encoded));
        assertFalse(passwordEncoder.matches("mypassword123", encoded));
    }

    @Test
    public void passwordEncoderWithUnicodeCharacters() {
        String unicodePassword = "пароль密码🔐";
        String encoded = passwordEncoder.encode(unicodePassword);
        assertTrue(passwordEncoder.matches(unicodePassword, encoded));
    }
}
