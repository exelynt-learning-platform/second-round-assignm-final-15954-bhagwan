package com.example.springbootapp.security;

import com.example.springbootapp.model.User;
import com.example.springbootapp.repository.UserRepository;
import com.example.springbootapp.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {
    @Mock
    UserRepository userRepository;

    @InjectMocks
    CustomUserDetailsService userDetailsService;

    @Test
    public void loadUserByUsernameReturnsValidUserDetails() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("hashedpassword");
        user.setEmail("test@example.com");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("hashedpassword", result.getPassword());
    }

    @Test
    public void loadUserByUsernameThrowsExceptionWhenUserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
            userDetailsService.loadUserByUsername("nonexistent")
        );
    }

    @Test
    public void loadUserByUsernameCallsFindByUsername() {
        User user = new User();
        user.setUsername("user123");
        user.setPassword("password");
        when(userRepository.findByUsername("user123")).thenReturn(Optional.of(user));

        userDetailsService.loadUserByUsername("user123");

        verify(userRepository, times(1)).findByUsername("user123");
    }
}
