package com.example.springbootapp.service;

import com.example.springbootapp.model.Role;
import com.example.springbootapp.model.User;
import com.example.springbootapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) throw new IllegalArgumentException("Username exists");
        if (userRepository.existsByEmail(email)) throw new IllegalArgumentException("Email exists");
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(password));
        HashSet<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);
        u.setRoles(roles);
        return userRepository.save(u);
    }
}
