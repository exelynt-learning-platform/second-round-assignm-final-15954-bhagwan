package com.example.springbootapp.exception;

import com.example.springbootapp.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerExtendedTest {
    @InjectMocks
    GlobalExceptionHandler handler;

    @Test
    public void handleNotFoundException() {
        NoSuchElementException ex = new NoSuchElementException("Resource not found");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/resource/1");

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex, request);

        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Not Found", response.getBody().getError());
    }

    @Test
    public void handleBadRequestException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid value");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/resource");

        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(ex, request);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Bad Request", response.getBody().getError());
    }

    @Test
    public void handleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/admin");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(ex, request);

        assertEquals(403, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Forbidden", response.getBody().getError());
    }

    @Test
    public void handleGenericException() {
        Exception ex = new Exception("Unexpected error");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/any");

        ResponseEntity<ErrorResponse> response = handler.handleGeneric(ex, request);

        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Internal Server Error", response.getBody().getError());
    }

    @Test
    public void handleNullPointerException() {
        NullPointerException ex = new NullPointerException("Null reference");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<ErrorResponse> response = handler.handleGeneric(ex, request);

        assertEquals(500, response.getStatusCode().value());
    }
}
