package com.example.springbootapp.service;

import com.example.springbootapp.model.Role;
import com.example.springbootapp.model.User;
import com.example.springbootapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @Captor
    ArgumentCaptor<User> userCaptor;

    @Test
    public void registerNewUserSuccess() {
        userService = new UserService(userRepository, new BCryptPasswordEncoder());
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("a@b.com")).thenReturn(false);
        User saved = new User(); 
        saved.setId(1L); 
        saved.setUsername("alice");
        when(userRepository.save(any())).thenReturn(saved);

        User u = userService.register("alice", "a@b.com", "secret");
        assertNotNull(u);
        assertEquals("alice", u.getUsername());
        verify(userRepository).save(userCaptor.capture());
        User captured = userCaptor.getValue();
        assertTrue(captured.getPassword().length() > 0);
        assertTrue(captured.getRoles().contains(Role.ROLE_USER));
    }

    @Test
    public void registerFailsWhenUsernameExists() {
        userService = new UserService(userRepository, new BCryptPasswordEncoder());
        when(userRepository.existsByUsername("bob")).thenReturn(true);

        assertThrows(RuntimeException.class, () ->
            userService.register("bob", "bob@example.com", "password")
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    public void registerFailsWhenEmailExists() {
        userService = new UserService(userRepository, new BCryptPasswordEncoder());
        when(userRepository.existsByUsername("charlie")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () ->
            userService.register("charlie", "existing@example.com", "password")
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    public void registerEncodesPasswordCorrectly() {
        userService = new UserService(userRepository, new BCryptPasswordEncoder());
        when(userRepository.existsByUsername("david")).thenReturn(false);
        when(userRepository.existsByEmail("david@example.com")).thenReturn(false);
        User saved = new User();
        saved.setId(2L);
        saved.setUsername("david");
        when(userRepository.save(any())).thenReturn(saved);

        userService.register("david", "david@example.com", "mypassword123");

        verify(userRepository).save(userCaptor.capture());
        User captured = userCaptor.getValue();
        assertNotEquals("mypassword123", captured.getPassword()); // Should be encoded
        assertTrue(captured.getPassword().startsWith("$2a$") || captured.getPassword().startsWith("$2b$")); // BCrypt hash
    }

    @Test
    public void registerAssignsUserRole() {
        userService = new UserService(userRepository, new BCryptPasswordEncoder());
        when(userRepository.existsByUsername("eve")).thenReturn(false);
        when(userRepository.existsByEmail("eve@example.com")).thenReturn(false);
        User saved = new User();
        saved.setId(3L);
        saved.setUsername("eve");
        when(userRepository.save(any())).thenReturn(saved);

        userService.register("eve", "eve@example.com", "password");

        verify(userRepository).save(userCaptor.capture());
        User captured = userCaptor.getValue();
        assertNotNull(captured.getRoles());
        assertTrue(captured.getRoles().contains(Role.ROLE_USER));
    }
}
