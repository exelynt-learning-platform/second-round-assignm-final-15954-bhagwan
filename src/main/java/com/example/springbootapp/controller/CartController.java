package com.example.springbootapp.controller;

import com.example.springbootapp.model.*;
import com.example.springbootapp.repository.CartRepository;
import com.example.springbootapp.repository.ProductRepository;
import com.example.springbootapp.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartController(CartRepository cartRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    public ResponseEntity<?> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        var user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(cartRepository.findByUser(user).orElseGet(() -> {
            Cart c = new Cart(); c.setUser(user); return cartRepository.save(c);
        }));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@AuthenticationPrincipal UserDetails userDetails, @RequestParam Long productId, @RequestParam int qty) {
        // Input validation
        if (productId == null || productId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid product ID"));
        }
        if (qty <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Quantity must be greater than 0"));
        }
        
        var user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> { 
            Cart c = new Cart(); 
            c.setUser(user); 
            return cartRepository.save(c); 
        });
        
        // Get product
        Optional<Product> prodOpt = productRepository.findById(productId);
        if (prodOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Product product = prodOpt.get();
        
        // Calculate total quantity already in cart for this product
        int quantityInCart = cart.getItems().stream()
            .filter(item -> item.getProduct().getId().equals(productId))
            .mapToInt(CartItem::getQuantity)
            .sum();
        
        int totalQuantity = quantityInCart + qty;
        
        // Validate stock availability BEFORE creating CartItem
        // Check total quantity (existing + requested) against available stock
        if (product.getStockQuantity() < totalQuantity) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Not enough stock",
                "available", product.getStockQuantity(),
                "alreadyInCart", quantityInCart,
                "requested", qty,
                "total", totalQuantity
            ));
        }
        
        // Check if product already exists in cart - if so, update quantity
        boolean found = false;
        for (CartItem existingItem : cart.getItems()) {
            if (existingItem.getProduct().getId().equals(productId)) {
                existingItem.setQuantity(existingItem.getQuantity() + qty);
                found = true;
                break;
            }
        }
        
        // If product not in cart, create new cart item with proper initialization
        if (!found) {
            CartItem item = new CartItem(); 
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(qty);
            cart.getItems().add(item);
        }
        
        Cart saved = cartRepository.save(cart);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeFromCart(@AuthenticationPrincipal UserDetails userDetails, @RequestParam Long cartItemId) {
        // Input validation
        if (cartItemId == null || cartItemId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid cart item ID"));
        }
        
        try {
            var user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
            Cart cart = cartRepository.findByUser(user).orElseThrow();
            
            boolean removed = cart.getItems().removeIf(i -> Objects.equals(i.getId(), cartItemId));
            if (!removed) {
                return ResponseEntity.notFound().build();
            }
            
            cartRepository.save(cart);
            return ResponseEntity.ok(Map.of("message", "Item removed from cart"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to remove item: " + e.getMessage()));
        }
    }
}
