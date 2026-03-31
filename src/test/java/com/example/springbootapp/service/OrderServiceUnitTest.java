package com.example.springbootapp.service;

import com.example.springbootapp.model.*;
import com.example.springbootapp.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceUnitTest {
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
    OrderService orderService;

    @Test
    public void testCreateOrderWithValidCart() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");

        Product product = new Product();
        product.setId(1L);
        product.setPrice(100.0);
        product.setStockQuantity(10);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setItems(new ArrayList<>(List.of(item)));

        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setPaymentStatus("PENDING");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any())).thenReturn(order);

        OrderEntity result = orderService.createOrderWithPayment("alice", "123 Main St");

        assertNotNull(result);
        verify(orderRepository).save(any());
    }

    @Test
    public void testCreateOrderThrowsOnEmptyCart() {
        User user = new User();
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        assertThrows(IllegalArgumentException.class, () ->
            orderService.createOrderWithPayment("bob", "123 Main St")
        );
    }

    @Test
    public void testUpdateStockSuccessfully() {
        Product product = new Product();
        product.setStockQuantity(10);

        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(1L);
        orderItem.setQuantity(3);

        OrderEntity order = new OrderEntity();
        order.setItems(List.of(orderItem));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        orderService.updateStockForConfirmedOrder(1L);

        assertEquals(7, product.getStockQuantity());
    }

    @Test
    public void testClearCartForUser() {
        User user = new User();
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>(List.of(new CartItem())));

        when(userRepository.findByUsername("charlie")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        orderService.clearCartForUser("charlie");

        assertTrue(cart.getItems().isEmpty());
    }
}
