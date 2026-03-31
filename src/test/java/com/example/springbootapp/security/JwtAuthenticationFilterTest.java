package com.example.springbootapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {
    @Mock
    JwtUtil jwtUtil;
    @Mock
    UserDetailsService userDetailsService;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    FilterChain filterChain;
    @Mock
    UserDetails userDetails;

    @InjectMocks
    JwtAuthenticationFilter filter;

    @Test
    public void doFilterInternalWithValidToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer validtoken123");
        when(jwtUtil.validateToken("validtoken123")).thenReturn(true);
        when(jwtUtil.getUsernameFromToken("validtoken123")).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);

        SecurityContextHolder.clearContext();
        filter.doFilterInternal(request, response, filterChain);

        verify(jwtUtil).validateToken("validtoken123");
        verify(jwtUtil).getUsernameFromToken("validtoken123");
        verify(userDetailsService).loadUserByUsername("testuser");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void doFilterInternalWithInvalidToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidtoken");
        when(jwtUtil.validateToken("invalidtoken")).thenReturn(false);

        SecurityContextHolder.clearContext();
        filter.doFilterInternal(request, response, filterChain);

        verify(jwtUtil).validateToken("invalidtoken");
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void doFilterInternalWithoutAuthorizationHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        SecurityContextHolder.clearContext();
        filter.doFilterInternal(request, response, filterChain);

        verify(jwtUtil, never()).validateToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void doFilterInternalWithNonBearerToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic xyz");

        SecurityContextHolder.clearContext();
        filter.doFilterInternal(request, response, filterChain);

        verify(jwtUtil, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void doFilterInternalWithEmptyAuthorizationHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("");

        SecurityContextHolder.clearContext();
        filter.doFilterInternal(request, response, filterChain);

        verify(jwtUtil, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
    }
}
