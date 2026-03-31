package com.example.springbootapp.controller;

import com.example.springbootapp.model.*;
import com.example.springbootapp.repository.OrderRepository;
import com.example.springbootapp.repository.UserRepository;
import com.example.springbootapp.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;

    public OrderController(OrderRepository orderRepository, UserRepository userRepository, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String shippingAddress) {
        try {
            OrderEntity order = orderService.createOrderWithPayment(userDetails.getUsername(), shippingAddress);
            return ResponseEntity.status(201).body(Map.of(
                "orderId", order.getId(),
                "paymentUrl", "/api/payments/checkout?orderId=" + order.getId(),
                "totalPrice", order.getTotalPrice()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to create order: " + e.getMessage()));
        }
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
