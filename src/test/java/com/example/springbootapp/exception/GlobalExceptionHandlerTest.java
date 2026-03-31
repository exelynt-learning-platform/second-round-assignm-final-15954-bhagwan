package com.example.springbootapp.exception;

import com.example.springbootapp.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {
    @Mock
    HttpServletRequest request;

    @InjectMocks
    GlobalExceptionHandler handler;

    @Test
    public void handleNotFound() {
        when(request.getRequestURI()).thenReturn("/api/test");
        NoSuchElementException ex = new NoSuchElementException("Resource not found");

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("Resource not found", response.getBody().getMessage());
    }

    @Test
    public void handleBadRequest() {
        when(request.getRequestURI()).thenReturn("/api/test");
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input");

        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Invalid input", response.getBody().getMessage());
    }

    @Test
    public void handleAccessDenied() {
        when(request.getRequestURI()).thenReturn("/api/admin");
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Forbidden", response.getBody().getError());
    }
}
