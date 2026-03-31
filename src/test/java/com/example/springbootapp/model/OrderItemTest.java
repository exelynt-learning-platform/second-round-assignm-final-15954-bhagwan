package com.example.springbootapp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OrderItemTest {
    @Test
    public void orderItemGettersSetters() {
        OrderItem item = new OrderItem();
        
        item.setId(1L);
        assertEquals(1L, item.getId());
        
        item.setProductId(5L);
        assertEquals(5L, item.getProductId());
        
        item.setProductName("Test Product");
        assertEquals("Test Product", item.getProductName());
        
        item.setUnitPrice(99.99);
        assertEquals(99.99, item.getUnitPrice());
        
        item.setQuantity(3);
        assertEquals(3, item.getQuantity());
    }
}
