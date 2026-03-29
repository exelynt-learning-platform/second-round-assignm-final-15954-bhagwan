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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    @Mock
    UserService userService;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JwtUtil jwtUtil;
    @Mock
    Authentication authentication;

    @InjectMocks
    AuthController controller;

    @Test
    public void registerCreatesNewUser() {
        User newUser = new User();
        newUser.setId(1L);
        newUser.setUsername("alice");
        when(userService.register("alice", "alice@example.com", "password123")).thenReturn(newUser);

        Map<String, String> body = Map.of("username", "alice", "email", "alice@example.com", "password", "password123");
        ResponseEntity<?> response = controller.register(body);
        
        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(userService, times(1)).register("alice", "alice@example.com", "password123");
    }

    @Test
    public void loginReturnsTokenForValidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtUtil.generateToken("bob")).thenReturn("jwt_token_here");

        Map<String, String> body = Map.of("username", "bob", "password", "password123");
        ResponseEntity<?> response = controller.login(body);
        
        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtUtil, times(1)).generateToken("bob");
    }

    @Test
    public void loginHandlesAuthenticationFailure() {
        when(authenticationManager.authenticate(any())).thenThrow(new org.springframework.security.core.AuthenticationException("Invalid credentials"){});

        Map<String, String> body = Map.of("username", "user", "password", "wrongpassword");
        
        assertThrows(Exception.class, () -> controller.login(body));
    }
}
