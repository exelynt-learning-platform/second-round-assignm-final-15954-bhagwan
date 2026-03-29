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

import java.util.ArrayList;
import java.util.Arrays;
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
        User u = new User();
        u.setId(1L);
        u.setUsername("bob");

        Product p = new Product();
        p.setId(5L);
        p.setName("Prod");
        p.setStockQuantity(10);

        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(u));
        when(cartRepository.findByUser(u)).thenReturn(Optional.empty());
        when(productRepository.findById(5L)).thenReturn(Optional.of(p));

        Cart saved = new Cart();
        saved.setId(2L);
        saved.setUser(u);
        saved.setItems(new ArrayList<>());

        when(cartRepository.save(any())).thenReturn(saved);

        UserDetails ud = org.springframework.security.core.userdetails.User.withUsername("bob").password("x").authorities("ROLE_USER").build();
        var resp = controller.addToCart(ud, 5L, 2);

        assertTrue(resp.getStatusCode().is2xxSuccessful());
        verify(cartRepository, times(2)).save(any());
    }

    @Test
    public void addToCartAddsItemToExistingCart() {
        User u = new User();
        u.setId(1L);
        u.setUsername("alice");

        Product p = new Product();
        p.setId(10L);
        p.setName("Widget");
        p.setStockQuantity(20);

        Cart cart = new Cart();
        cart.setId(5L);
        cart.setUser(u);
        cart.setItems(new ArrayList<>());

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(u));
        when(cartRepository.findByUser(u)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(p));
        when(cartRepository.save(any())).thenReturn(cart);

        UserDetails ud = org.springframework.security.core.userdetails.User.withUsername("alice").password("x").authorities("ROLE_USER").build();
        var resp = controller.addToCart(ud, 10L, 3);

        assertTrue(resp.getStatusCode().is2xxSuccessful());
        verify(cartRepository, times(1)).save(any());
    }

    @Test
    public void addToCartFailsWithInvalidProduct() {
        User u = new User();
        u.setId(1L);
        u.setUsername("charlie");

        when(userRepository.findByUsername("charlie")).thenReturn(Optional.of(u));
        when(cartRepository.findByUser(u)).thenReturn(Optional.empty());
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        UserDetails ud = org.springframework.security.core.userdetails.User.withUsername("charlie").password("x").authorities("ROLE_USER").build();
        var resp = controller.addToCart(ud, 999L, 1);

        assertEquals(404, resp.getStatusCode().value());
    }

    @Test
    public void addToCartFailsWithInsufficientStock() {
        User u = new User();
        u.setId(1L);
        u.setUsername("david");

        Product p = new Product();
        p.setId(5L);
        p.setName("Product");
        p.setStockQuantity(2); // Only 2 in stock

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(u);
        cart.setItems(new ArrayList<>());

        when(userRepository.findByUsername("david")).thenReturn(Optional.of(u));
        when(cartRepository.findByUser(u)).thenReturn(Optional.of(cart));
        when(productRepository.findById(5L)).thenReturn(Optional.of(p));

        UserDetails ud = org.springframework.security.core.userdetails.User.withUsername("david").password("x").authorities("ROLE_USER").build();
        var resp = controller.addToCart(ud, 5L, 5); // Trying to add 5 when only 2 available

        assertEquals(400, resp.getStatusCode().value());
    }

    @Test
    public void getCartReturnsExistingCart() {
        User u = new User();
        u.setId(1L);
        u.setUsername("eve");

        Cart cart = new Cart();
        cart.setId(3L);
        cart.setUser(u);

        when(userRepository.findByUsername("eve")).thenReturn(Optional.of(u));
        when(cartRepository.findByUser(u)).thenReturn(Optional.of(cart));

        UserDetails ud = org.springframework.security.core.userdetails.User.withUsername("eve").password("x").authorities("ROLE_USER").build();
        var resp = controller.getCart(ud);

        assertTrue(resp.getStatusCode().is2xxSuccessful());
        assertEquals(cart, resp.getBody());
    }

    @Test
    public void getCartCreatesNewCartIfNotExists() {
        User u = new User();
        u.setId(1L);
        u.setUsername("frank");

        Cart newCart = new Cart();
        newCart.setId(4L);
        newCart.setUser(u);
        newCart.setItems(new ArrayList<>());

        when(userRepository.findByUsername("frank")).thenReturn(Optional.of(u));
        when(cartRepository.findByUser(u)).thenReturn(Optional.empty());
        when(cartRepository.save(any())).thenReturn(newCart);

        UserDetails ud = org.springframework.security.core.userdetails.User.withUsername("frank").password("x").authorities("ROLE_USER").build();
        var resp = controller.getCart(ud);

        assertTrue(resp.getStatusCode().is2xxSuccessful());
        verify(cartRepository, times(1)).save(any());
    }

    @Test
    public void removeFromCartRemovesItem() {
        User u = new User();
        u.setId(1L);
        u.setUsername("grace");

        Product p = new Product();
        p.setId(5L);

        CartItem item = new CartItem();
        item.setId(10L);
        item.setProduct(p);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(u);
        cart.setItems(new ArrayList<>(Arrays.asList(item)));

        when(userRepository.findByUsername("grace")).thenReturn(Optional.of(u));
        when(cartRepository.findByUser(u)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenReturn(cart);

        UserDetails ud = org.springframework.security.core.userdetails.User.withUsername("grace").password("x").authorities("ROLE_USER").build();
        var resp = controller.removeFromCart(ud, 10L);

        assertTrue(resp.getStatusCode().is2xxSuccessful());
        assertEquals(0, cart.getItems().size());
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    public void removeFromCartHandlesNonExistentItem() {
        User u = new User();
        u.setId(1L);
        u.setUsername("henry");

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(u);
        cart.setItems(new ArrayList<>());

        when(userRepository.findByUsername("henry")).thenReturn(Optional.of(u));
        when(cartRepository.findByUser(u)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenReturn(cart);

        UserDetails ud = org.springframework.security.core.userdetails.User.withUsername("henry").password("x").authorities("ROLE_USER").build();
        var resp = controller.removeFromCart(ud, 999L);

        assertTrue(resp.getStatusCode().is2xxSuccessful());
        verify(cartRepository, times(1)).save(cart);
    }
}
