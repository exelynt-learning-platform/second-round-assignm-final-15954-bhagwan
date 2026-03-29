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
        User saved = new User(); saved.setId(1L); saved.setUsername("alice");
        when(userRepository.save(any())).thenReturn(saved);

        User u = userService.register("alice", "a@b.com", "secret");
        assertNotNull(u);
        assertEquals("alice", u.getUsername());
        verify(userRepository).save(userCaptor.capture());
        User captured = userCaptor.getValue();
        assertTrue(captured.getPassword().length() > 0);
        assertTrue(captured.getRoles().contains(Role.ROLE_USER));
    }
}
