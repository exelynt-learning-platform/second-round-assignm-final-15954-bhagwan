package com.example.springbootapp.model;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class OrderEntityTest {
    @Test
    public void orderEntityGettersSetters() {
        OrderEntity order = new OrderEntity();
        
        order.setId(1L);
        assertEquals(1L, order.getId());
        
        User user = new User();
        user.setId(1L);
        order.setUser(user);
        assertEquals(user, order.getUser());
        
        List<OrderItem> items = new ArrayList<>();
        order.setItems(items);
        assertEquals(items, order.getItems());
        
        order.setTotalPrice(99.99);
        assertEquals(99.99, order.getTotalPrice());
        
        order.setShippingAddress("123 Main St");
        assertEquals("123 Main St", order.getShippingAddress());
        
        order.setPaymentStatus("PENDING");
        assertEquals("PENDING", order.getPaymentStatus());
        
        order.setPaymentSessionId("sess_123");
        assertEquals("sess_123", order.getPaymentSessionId());
        
        order.setPaymentIntentId("pi_123");
        assertEquals("pi_123", order.getPaymentIntentId());
        
        order.setVersion(1L);
        assertEquals(1L, order.getVersion());
        
        Instant now = Instant.now();
        order.setCreatedAt(now);
        assertEquals(now, order.getCreatedAt());
    }
}
