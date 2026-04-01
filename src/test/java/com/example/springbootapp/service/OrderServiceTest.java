package com.example.springbootapp.service;

import com.example.springbootapp.model.*;
import com.example.springbootapp.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(99.99);
        product.setStockQuantity(100);

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);

        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cart.getItems().add(cartItem);
    }

    @Test
    void testCreateOrderWithPayment() {
        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setUser(user);
        order.setTotalPrice(199.98);
        order.setPaymentStatus(PaymentStatus.PENDING);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(order);

        assertDoesNotThrow(() -> orderService.createOrderWithPayment("testuser", "123 Main St"));
        verify(orderRepository, times(1)).save(any(OrderEntity.class));
    }

    @Test
    void testCreateOrderWithPaymentEmptyCart() {
        Cart emptyCart = new Cart();
        emptyCart.setUser(user);
        emptyCart.setItems(new ArrayList<>());

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(emptyCart));

        assertThrows(IllegalArgumentException.class, 
            () -> orderService.createOrderWithPayment("testuser", "123 Main St"));
    }

    @Test
    void testUpdateStockForConfirmedOrder() {
        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setUser(user);

        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(1L);
        orderItem.setProductName("Test Product");
        orderItem.setUnitPrice(99.99);
        orderItem.setQuantity(2);
        List<OrderItem> items = new ArrayList<>();
        items.add(orderItem);
        order.setItems(items);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        assertDoesNotThrow(() -> orderService.updateStockForConfirmedOrder(1L));
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testClearCartForUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        assertDoesNotThrow(() -> orderService.clearCartForUser("testuser"));
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testCreateOrderWithPaymentInsufficientStock() {
        product.setStockQuantity(1); // Only 1 in stock but need 2
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        assertThrows(IllegalArgumentException.class, 
            () -> orderService.createOrderWithPayment("testuser", "123 Main St"));
        verify(orderRepository, never()).save(any(OrderEntity.class));
    }

    @Test
    void testCreateOrderUserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, 
            () -> orderService.createOrderWithPayment("nonexistent", "123 Main St"));
    }

    @Test
    void testClearCartUserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, 
            () -> orderService.clearCartForUser("nonexistent"));
    }

    @Test
    void testUpdateStockOrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, 
            () -> orderService.updateStockForConfirmedOrder(999L));
    }
}
