package com.example.springbootapp.controller;

import com.example.springbootapp.model.*;
import com.example.springbootapp.repository.CartRepository;
import com.example.springbootapp.repository.OrderRepository;
import com.example.springbootapp.repository.ProductRepository;
import com.example.springbootapp.repository.UserRepository;
import com.example.springbootapp.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PaymentService paymentService;

    public OrderController(CartRepository cartRepository, UserRepository userRepository, OrderRepository orderRepository, ProductRepository productRepository, PaymentService paymentService) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String shippingAddress) {
        var user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Cart cart = cartRepository.findByUser(user).orElseThrow();
        if (cart.getItems().isEmpty()) return ResponseEntity.badRequest().body("Cart is empty");
        OrderEntity order = new OrderEntity();
        order.setUser(user);
        var items = cart.getItems().stream().map(ci -> {
            OrderItem oi = new OrderItem();
            oi.setProductId(ci.getProduct().getId());
            oi.setProductName(ci.getProduct().getName());
            oi.setUnitPrice(ci.getProduct().getPrice());
            oi.setQuantity(ci.getQuantity());
            return oi;
        }).collect(Collectors.toList());
        order.setItems(items);
        double total = items.stream().mapToDouble(i -> i.getUnitPrice() * i.getQuantity()).sum();
        order.setTotalPrice(total);
        order.setShippingAddress(shippingAddress);
        order.setPaymentStatus("PENDING");
        OrderEntity saved = orderRepository.save(order);
        // Optionally clear cart
        cart.getItems().clear();
        cartRepository.save(cart);
        // Create payment intent
        String paymentUrl = paymentService.createPayment(saved);
        return ResponseEntity.status(201).body(Map.of("orderId", saved.getId(), "paymentUrl", paymentUrl));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        var user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        return orderRepository.findById(id).map(o -> {
            if (!o.getUser().getId().equals(user.getId())) return ResponseEntity.status(403).build();
            return ResponseEntity.ok(o);
        }).orElse(ResponseEntity.notFound().build());
    }
}
