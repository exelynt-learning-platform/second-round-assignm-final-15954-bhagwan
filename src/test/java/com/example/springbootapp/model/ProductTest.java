package com.example.springbootapp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ProductTest {
    @Test
    public void productGettersSetters() {
        Product product = new Product();
        
        product.setId(1L);
        assertEquals(1L, product.getId());
        
        product.setName("Test Product");
        assertEquals("Test Product", product.getName());
        
        product.setDescription("Product description");
        assertEquals("Product description", product.getDescription());
        
        product.setPrice(99.99);
        assertEquals(99.99, product.getPrice());
        
        product.setStockQuantity(100);
        assertEquals(100, product.getStockQuantity());
    }
}
