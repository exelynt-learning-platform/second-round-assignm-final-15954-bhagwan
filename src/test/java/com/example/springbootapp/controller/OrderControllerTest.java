package com.example.springbootapp.controller;

import com.example.springbootapp.model.*;
import com.example.springbootapp.repository.*;
import com.example.springbootapp.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {
    @Mock
    CartRepository cartRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    OrderRepository orderRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    PaymentService paymentService;

    @InjectMocks
    OrderController controller;

    @Test
    public void createOrderSuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user1");

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(100.0);
        product.setStockQuantity(10);

        CartItem item = new CartItem();
        item.setId(1L);
        item.setProduct(product);
        item.setQuantity(2);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setItems(new ArrayList<>(Arrays.asList(item)));

        OrderEntity savedOrder = new OrderEntity();
        savedOrder.setId(1L);
        savedOrder.setUser(user);

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any())).thenReturn(savedOrder);
        when(paymentService.createPayment(savedOrder)).thenReturn("https://payment.url");

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("user1").password("x").authorities("ROLE_USER").build();
        ResponseEntity<?> response = controller.createOrder(userDetails, "123 Main St");

        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(orderRepository).save(any());
        verify(paymentService).createPayment(savedOrder);
    }

    @Test
    public void createOrderFailsWithEmptyCart() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user1");

        Cart empty_cart = new Cart();
        empty_cart.setId(1L);
        empty_cart.setUser(user);
        empty_cart.setItems(new ArrayList<>());

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(empty_cart));

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("user1").password("x").authorities("ROLE_USER").build();
        ResponseEntity<?> response = controller.createOrder(userDetails, "123 Main St");

        assertEquals(400, response.getStatusCode().value());
        verify(orderRepository, never()).save(any());
    }

    @Test
    public void createOrderFailsWithInsufficientStock() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user1");

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(100.0);
        product.setStockQuantity(1); // Only 1 in stock

        CartItem item = new CartItem();
        item.setId(1L);
        item.setProduct(product);
        item.setQuantity(5); // Trying to order 5

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setItems(Arrays.asList(item));

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("user1").password("x").authorities("ROLE_USER").build();
        ResponseEntity<?> response = controller.createOrder(userDetails, "123 Main St");

        assertEquals(400, response.getStatusCode().value());
        verify(orderRepository, never()).save(any());
    }

    @Test
    public void getOrderReturnsOrderForOwner() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user1");

        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setUser(user);

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("user1").password("x").authorities("ROLE_USER").build();
        ResponseEntity<?> response = controller.getOrder(userDetails, 1L);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(order, response.getBody());
    }

    @Test
    public void getOrderDeniesAccessForNonOwner() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setUser(user2); // Order belongs to user2

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("user1").password("x").authorities("ROLE_USER").build();
        ResponseEntity<?> response = controller.getOrder(userDetails, 1L);

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    public void getOrderReturnsNotFoundForNonExistentOrder() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user1");

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("user1").password("x").authorities("ROLE_USER").build();
        ResponseEntity<?> response = controller.getOrder(userDetails, 999L);

        assertEquals(404, response.getStatusCode().value());
    }
}
