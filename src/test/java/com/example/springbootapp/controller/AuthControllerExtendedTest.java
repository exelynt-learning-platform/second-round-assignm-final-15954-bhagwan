package com.example.springbootapp.controller;

import com.example.springbootapp.model.User;
import com.example.springbootapp.security.JwtUtil;
import com.example.springbootapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerExtendedTest {
    @Mock
    UserService userService;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JwtUtil jwtUtil;

    @InjectMocks
    AuthController controller;

    @Test
    public void registerWithNullUsername() {
        Map<String, String> request = new HashMap<>();
        request.put("username", null);
        request.put("email", "test@example.com");
        request.put("password", "password123");

        ResponseEntity<?> response = controller.register(request);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void registerWithBlankEmail() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("email", "");
        request.put("password", "password123");

        ResponseEntity<?> response = controller.register(request);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void loginWithNullPassword() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("password", null);

        ResponseEntity<?> response = controller.login(request);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void registerWithShortPassword() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "newuser");
        request.put("email", "new@example.com");
        request.put("password", "short");

        ResponseEntity<?> response = controller.register(request);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void loginWithNullUsername() {
        Map<String, String> request = new HashMap<>();
        request.put("username", null);
        request.put("password", "password123");

        ResponseEntity<?> response = controller.login(request);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void registerSuccessfully() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@example.com");
        
        when(userService.register("testuser", "test@example.com", "password123"))
            .thenReturn(mockUser);

        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("email", "test@example.com");
        request.put("password", "password123");

        ResponseEntity<?> response = controller.register(request);

        assertEquals(201, response.getStatusCode().value());
        verify(userService, times(1)).register("testuser", "test@example.com", "password123");
    }

    @Test
    public void loginWithBadCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Wrong password"));

        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("password", "wrongpassword");

        assertThrows(BadCredentialsException.class, () -> controller.login(request));
    }

    @Test
    public void registerWithDuplicateUser() {
        when(userService.register("testuser", "test@example.com", "password123"))
            .thenThrow(new IllegalArgumentException("Username exists"));

        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("email", "test@example.com");
        request.put("password", "password123");

        assertThrows(IllegalArgumentException.class, () -> controller.register(request));
    }
}
