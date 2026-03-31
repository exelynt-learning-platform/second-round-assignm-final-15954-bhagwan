package com.example.springbootapp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    @Test
    public void userGettersSetters() {
        User user = new User();
        
        user.setId(1L);
        assertEquals(1L, user.getId());
        
        user.setUsername("testuser");
        assertEquals("testuser", user.getUsername());
        
        user.setEmail("test@example.com");
        assertEquals("test@example.com", user.getEmail());
        
        user.setPassword("hashedpassword");
        assertEquals("hashedpassword", user.getPassword());
        
        java.util.Set<Role> roles = new java.util.HashSet<>();
        roles.add(Role.ROLE_USER);
        user.setRoles(roles);
        assertTrue(user.getRoles().contains(Role.ROLE_USER));
    }
}
