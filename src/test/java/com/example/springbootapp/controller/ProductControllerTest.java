package com.example.springbootapp.controller;

import com.example.springbootapp.model.Product;
import com.example.springbootapp.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {
    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductController controller;

    @Test
    public void listReturnsProducts() {
        Product p = new Product(); 
        p.setId(1L); 
        p.setName("X");
        when(productRepository.findAll()).thenReturn(List.of(p));
        var res = controller.list();
        assertEquals(1, res.size());
    }

    @Test
    public void getReturnsProduct() {
        Product p = new Product(); 
        p.setId(1L); 
        p.setName("X");
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        var resp = controller.get(1L);
        assertTrue(resp.getStatusCode().is2xxSuccessful());
        assertEquals(p, resp.getBody());
    }

    @Test
    public void getReturnsNotFoundForMissingProduct() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        var resp = controller.get(999L);
        assertEquals(404, resp.getStatusCode().value());
    }

    @Test
    public void listReturnsEmptyWhenNoProducts() {
        when(productRepository.findAll()).thenReturn(List.of());
        var res = controller.list();
        assertEquals(0, res.size());
    }

    @Test
    public void listReturnsMultipleProducts() {
        Product p1 = new Product();
        p1.setId(1L);
        p1.setName("Product 1");
        
        Product p2 = new Product();
        p2.setId(2L);
        p2.setName("Product 2");
        
        Product p3 = new Product();
        p3.setId(3L);
        p3.setName("Product 3");
        
        when(productRepository.findAll()).thenReturn(List.of(p1, p2, p3));
        var res = controller.list();
        assertEquals(3, res.size());
    }

    @Test
    public void getReturnsCorrectProduct() {
        Product p1 = new Product();
        p1.setId(1L);
        p1.setName("Product One");
        p1.setPrice(99.99);
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(p1));
        var resp = controller.get(1L);
        
        assertTrue(resp.getStatusCode().is2xxSuccessful());
        assertEquals("Product One", resp.getBody().getName());
        assertEquals(99.99, resp.getBody().getPrice());
    }

    @Test
    public void listCallsFindAll() {
        when(productRepository.findAll()).thenReturn(List.of());
        controller.list();
        verify(productRepository, times(1)).findAll();
    }

    @Test
    public void getCallsFindById() {
        Product p = new Product();
        p.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        controller.get(1L);
        verify(productRepository, times(1)).findById(1L);
    }
}
