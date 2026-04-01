package com.example.springbootapp.controller;

import com.example.springbootapp.model.*;
import com.example.springbootapp.repository.*;
import com.example.springbootapp.service.OrderService;
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
    OrderRepository orderRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    OrderService orderService;

    @InjectMocks
    OrderController controller;

    @Test
    public void createOrderSuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user1");

        OrderEntity savedOrder = new OrderEntity();
        savedOrder.setId(1L);
        savedOrder.setUser(user);
        savedOrder.setTotalPrice(200.0);
        savedOrder.setPaymentStatus(PaymentStatus.PENDING);

        when(orderService.createOrderWithPayment("user1", "123 Main St")).thenReturn(savedOrder);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("user1").password("x").authorities("ROLE_USER").build();
        ResponseEntity<?> response = controller.createOrder(userDetails, "123 Main St");

        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(orderService).createOrderWithPayment("user1", "123 Main St");
    }

    @Test
    public void createOrderFailsWithEmptyCart() {
        when(orderService.createOrderWithPayment("user1", "123 Main St"))
            .thenThrow(new IllegalArgumentException("Cart is empty"));

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("user1").password("x").authorities("ROLE_USER").build();
        ResponseEntity<?> response = controller.createOrder(userDetails, "123 Main St");

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void createOrderFailsWithInsufficientStock() {
        when(orderService.createOrderWithPayment("user1", "123 Main St"))
            .thenThrow(new IllegalArgumentException("Insufficient stock for product: Test Product"));

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("user1").password("x").authorities("ROLE_USER").build();
        ResponseEntity<?> response = controller.createOrder(userDetails, "123 Main St");

        assertEquals(400, response.getStatusCode().value());
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
