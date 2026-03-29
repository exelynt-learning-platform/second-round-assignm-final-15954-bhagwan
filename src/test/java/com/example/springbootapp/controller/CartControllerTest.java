package com.example.springbootapp.controller;

import com.example.springbootapp.model.Cart;
import com.example.springbootapp.model.CartItem;
import com.example.springbootapp.model.Product;
import com.example.springbootapp.model.User;
import com.example.springbootapp.repository.CartRepository;
import com.example.springbootapp.repository.ProductRepository;
import com.example.springbootapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartControllerTest {
    @Mock
    CartRepository cartRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ProductRepository productRepository;

    @InjectMocks
    CartController controller;

    @Test
    public void addToCartCreatesCartAndAddsItem() {
        User u = new User(); u.setId(1L); u.setUsername("bob");
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(u));
        when(cartRepository.findByUser(u)).thenReturn(Optional.empty());
        Product p = new Product(); p.setId(5L); p.setName("Prod"); p.setStockQuantity(10);
        when(productRepository.findById(5L)).thenReturn(Optional.of(p));
        Cart saved = new Cart(); saved.setId(2L);
        when(cartRepository.save(any())).thenReturn(saved);

        UserDetails ud = org.springframework.security.core.userdetails.User.withUsername("bob").password("x").authorities("ROLE_USER").build();
        var resp = controller.addToCart(ud, 5L, 2);
        assertTrue(resp.getStatusCode().is2xxSuccessful());
    }
}
