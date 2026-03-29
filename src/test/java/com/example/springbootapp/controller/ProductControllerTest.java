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
        Product p = new Product(); p.setId(1L); p.setName("X");
        when(productRepository.findAll()).thenReturn(List.of(p));
        var res = controller.list();
        assertEquals(1, res.size());
    }

    @Test
    public void getReturnsProduct() {
        Product p = new Product(); p.setId(1L); p.setName("X");
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        var resp = controller.get(1L);
        assertTrue(resp.getStatusCode().is2xxSuccessful());
        assertEquals(p, resp.getBody());
    }
}
