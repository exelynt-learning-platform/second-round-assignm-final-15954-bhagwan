package com.example.springbootapp.controller;

import com.example.springbootapp.model.*;
import com.example.springbootapp.repository.CartRepository;
import com.example.springbootapp.repository.ProductRepository;
import com.example.springbootapp.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
        var user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> { Cart c = new Cart(); c.setUser(user); return c; });
        Optional<Product> prodOpt = productRepository.findById(productId);
        if (prodOpt.isEmpty()) return ResponseEntity.notFound().build();
        Product p = prodOpt.get();
        if (p.getStockQuantity() < qty) return ResponseEntity.badRequest().body("Not enough stock");
        CartItem item = new CartItem(); item.setProduct(p); item.setQuantity(qty);
        cart.getItems().add(item);
        Cart saved = cartRepository.save(cart);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeFromCart(@AuthenticationPrincipal UserDetails userDetails, @RequestParam Long cartItemId) {
        var user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Cart cart = cartRepository.findByUser(user).orElseThrow();
        cart.getItems().removeIf(i -> i.getId() != null && i.getId().equals(cartItemId));
        cartRepository.save(cart);
        return ResponseEntity.ok().build();
    }
}
