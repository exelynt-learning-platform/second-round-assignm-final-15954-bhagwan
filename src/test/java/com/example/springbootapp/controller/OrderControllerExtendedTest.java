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
public class OrderControllerExtendedTest {
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
    public void getOrderWithUnauthorizedUser() {
        User owner = new User();
        owner.setId(1L);
        
        User other = new User();
        other.setId(2L);

        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setUser(owner);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(other));

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("other")
            .password("x").authorities("ROLE_USER").build();

        ResponseEntity<?> response = controller.getOrder(userDetails, 1L);

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    public void getOrderNotFoundReturns404() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("user1")
            .password("x").authorities("ROLE_USER").build();

        ResponseEntity<?> response = controller.getOrder(userDetails, 1L);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    public void getOrderByOwner() {
        User user = new User();
        user.setId(1L);

        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setUser(user);

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("user1")
            .password("x").authorities("ROLE_USER").build();

        ResponseEntity<?> response = controller.getOrder(userDetails, 1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(order, response.getBody());
    }

    @Test
    public void createOrderWithEmptyCart() {
        User user = new User();
        user.setId(1L);

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("user1")
            .password("x").authorities("ROLE_USER").build();

        ResponseEntity<?> response = controller.createOrder(userDetails, "123 Main St");

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void createOrderWithInsufficientStock() {
        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(1L);
        product.setName("Item");
        product.setStockQuantity(2);

        CartItem cartItem = new CartItem();
        cartItem.setQuantity(10);
        cartItem.setProduct(product);

        Cart cart = new Cart();
        cart.setItems(Arrays.asList(cartItem));

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("user1")
            .password("x").authorities("ROLE_USER").build();

        ResponseEntity<?> response = controller.createOrder(userDetails, "123 Main St");

        assertEquals(400, response.getStatusCode().value());
    }
}
