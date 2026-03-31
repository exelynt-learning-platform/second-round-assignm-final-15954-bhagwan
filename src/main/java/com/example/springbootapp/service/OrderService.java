package com.example.springbootapp.service;

import com.example.springbootapp.model.*;
import com.example.springbootapp.repository.CartRepository;
import com.example.springbootapp.repository.OrderRepository;
import com.example.springbootapp.repository.ProductRepository;
import com.example.springbootapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * OrderService handles order creation business logic with proper separation of concerns.
 * Manages cart validation, order creation, and payment initialization.
 * Stock updates are deferred to payment confirmation via webhook.
 */
@Service
public class OrderService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PaymentService paymentService;

    public OrderService(CartRepository cartRepository, UserRepository userRepository, 
                        OrderRepository orderRepository, ProductRepository productRepository, 
                        PaymentService paymentService) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.paymentService = paymentService;
    }

    /**
     * Creates an order from the user's cart and initiates payment.
     * Stock updates are deferred until payment confirmation to avoid race conditions.
     * 
     * @param username The authenticated user's username
     * @param shippingAddress The shipping address for the order
     * @return The created OrderEntity with payment URL
     * @throws IllegalArgumentException if cart is empty or stock is insufficient
     */
    @Transactional
    public OrderEntity createOrderWithPayment(String username, String shippingAddress) {
        // Load user and cart
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Cart cart = cartRepository.findByUser(user)
            .orElseThrow(() -> new IllegalArgumentException("Cart not found"));
        
        // Validate cart
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }
        
        // Validate stock availability for all items before creating order
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException(
                    "Insufficient stock for product: " + product.getName() + 
                    " (Available: " + product.getStockQuantity() + 
                    ", Requested: " + item.getQuantity() + ")"
                );
            }
        }
        
        // Create order from cart
        OrderEntity order = buildOrderFromCart(user, cart, shippingAddress);
        
        // Save order (payment status is PENDING)
        OrderEntity savedOrder = orderRepository.save(order);
        
        // Initiate payment (sets payment session ID and saves order again)
        paymentService.createPayment(savedOrder);
        
        // Clear the cart after successful order creation to prevent duplicate orders
        cart.getItems().clear();
        cartRepository.save(cart);
        
        // Note: Stock is NOT updated here to avoid race condition.
        // Stock will be updated by webhook handler after payment confirmation.
        
        return savedOrder;
    }

    /**
     * Updates product stock quantities after payment is confirmed.
     * Called from webhook handler to ensure stock is only decremented on successful payment.
     * 
     * @param orderId The ID of the confirmed order
     */
    @Transactional
    public void updateStockForConfirmedOrder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        // Update stock for all items in the order
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProductId()));
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
        }
    }

    /**
     * Clears the user's cart after successful payment.
     * 
     * @param username The authenticated user's username
     */
    @Transactional
    public void clearCartForUser(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Cart cart = cartRepository.findByUser(user)
            .orElseThrow(() -> new IllegalArgumentException("Cart not found"));
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    /**
     * Builds an OrderEntity from the user's cart.
     * 
     * @param user The user placing the order
     * @param cart The user's shopping cart
     * @param shippingAddress The shipping address
     * @return A new OrderEntity (not yet persisted)
     */
    private OrderEntity buildOrderFromCart(User user, Cart cart, String shippingAddress) {
        OrderEntity order = new OrderEntity();
        order.setUser(user);
        
        // Convert cart items to order items
        var items = cart.getItems().stream().map(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(cartItem.getProduct().getId());
            orderItem.setProductName(cartItem.getProduct().getName());
            orderItem.setUnitPrice(cartItem.getProduct().getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            return orderItem;
        }).collect(Collectors.toList());
        
        order.setItems(items);
        
        // Calculate total price
        double total = items.stream()
            .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
            .sum();
        order.setTotalPrice(total);
        
        order.setShippingAddress(shippingAddress);
        order.setPaymentStatus("PENDING");
        
        return order;
    }
}
