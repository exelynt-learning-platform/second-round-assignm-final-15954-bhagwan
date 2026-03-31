package com.example.springbootapp.dto;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

public class ErrorResponseTest {
    @Test
    public void errorResponseConstructorAndGettersSetters() {
        Instant time = Instant.now();
        ErrorResponse response = new ErrorResponse(time, 400, "Bad Request", "Invalid input", "/api/test");
        
        assertEquals(time, response.getTimestamp());
        assertEquals(400, response.getStatus());
        assertEquals("Bad Request", response.getError());
        assertEquals("Invalid input", response.getMessage());
        assertEquals("/api/test", response.getPath());
    }

    @Test
    public void errorResponseDefaultConstructor() {
        ErrorResponse response = new ErrorResponse();
        
        response.setTimestamp(Instant.now());
        response.setStatus(404);
        response.setError("Not Found");
        response.setMessage("Resource not found");
        response.setPath("/api/resource");
        
        assertNotNull(response.getTimestamp());
        assertEquals(404, response.getStatus());
        assertEquals("Not Found", response.getError());
        assertEquals("Resource not found", response.getMessage());
        assertEquals("/api/resource", response.getPath());
    }
}
