package com.example.springbootapp.model;

import com.example.springbootapp.dto.ErrorResponse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class EntityModelTest {

    @Test
    public void testProductEntity() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setDescription("Gaming Laptop");
        product.setPrice(999.99);
        product.setStockQuantity(50);

        assertEquals(1L, product.getId());
        assertEquals("Laptop", product.getName());
        assertEquals("Gaming Laptop", product.getDescription());
        assertEquals(999.99, product.getPrice());
        assertEquals(50, product.getStockQuantity());
    }

    @Test
    public void testUserEntity() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setEmail("test@example.com");
        user.setRoles(new HashSet<>(List.of(Role.ROLE_USER)));

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
        assertTrue(user.getRoles().contains(Role.ROLE_USER));
    }

    @Test
    public void testCartEntity() {
        User user = new User();
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setItems(new ArrayList<>());

        assertEquals(1L, cart.getId());
        assertEquals(user, cart.getUser());
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    public void testCartItemEntity() {
        Product product = new Product();
        CartItem item = new CartItem();
        item.setId(1L);
        item.setProduct(product);
        item.setQuantity(5);

        assertEquals(1L, item.getId());
        assertEquals(product, item.getProduct());
        assertEquals(5, item.getQuantity());
    }

    @Test
    public void testOrderEntity() {
        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setTotalPrice(299.99);
        order.setPaymentStatus("PENDING");
        order.setShippingAddress("123 Main St");
        order.setItems(new ArrayList<>());

        assertEquals(1L, order.getId());
        assertEquals(299.99, order.getTotalPrice());
        assertEquals("PENDING", order.getPaymentStatus());
        assertEquals("123 Main St", order.getShippingAddress());
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    public void testOrderItemEntity() {
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProductId(100L);
        item.setProductName("Widget");
        item.setQuantity(3);
        item.setUnitPrice(50.0);

        assertEquals(1L, item.getId());
        assertEquals(100L, item.getProductId());
        assertEquals("Widget", item.getProductName());
        assertEquals(3, item.getQuantity());
        assertEquals(50.0, item.getUnitPrice());
    }

    @Test
    public void testErrorResponseDTO() {
        ErrorResponse error = new ErrorResponse();
        error.setStatus(400);
        error.setMessage("Invalid input");

        assertEquals(400, error.getStatus());
        assertEquals("Invalid input", error.getMessage());
    }

    @Test
    public void testRoleEnum() {
        assertEquals(2, Role.values().length);
        assertEquals("ROLE_ADMIN", Role.ROLE_ADMIN.name());
        assertEquals("ROLE_USER", Role.ROLE_USER.name());
    }

    @Test
    public void testProductStockModification() {
        Product product = new Product();
        product.setStockQuantity(100);
        product.setStockQuantity(50);
        assertEquals(50, product.getStockQuantity());
    }

    @Test
    public void testOrderPaymentStatusProgression() {
        OrderEntity order = new OrderEntity();
        order.setPaymentStatus("PENDING");
        assertEquals("PENDING", order.getPaymentStatus());
    }

    @Test
    public void testCartItemsPersistence() {
        Cart cart = new Cart();
        CartItem item1 = new CartItem();
        CartItem item2 = new CartItem();
        cart.setItems(new ArrayList<>(List.of(item1, item2)));

        assertEquals(2, cart.getItems().size());
    }

    @Test
    public void testMultipleUserRoles() {
        User user = new User();
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);
        roles.add(Role.ROLE_ADMIN);
        user.setRoles(roles);

        assertEquals(2, user.getRoles().size());
    }

    @Test
    public void testOrderItemUnitPriceCalculation() {
        OrderItem item = new OrderItem();
        item.setUnitPrice(25.50);
        item.setQuantity(4);
        
        double total = item.getUnitPrice() * item.getQuantity();
        assertEquals(102.0, total, 0.01);
    }

    @Test
    public void testProductDescriptionNullable() {
        Product product = new Product();
        product.setName("Item");
        assertNull(product.getDescription());
    }

    @Test
    public void testLargeOrderAmount() {
        OrderEntity order = new OrderEntity();
        order.setTotalPrice(99999.99);
        assertEquals(99999.99, order.getTotalPrice());
    }

    @Test
    public void testUserEmailUpdate() {
        User user = new User();
        user.setEmail("old@example.com");
        user.setEmail("new@example.com");
        assertEquals("new@example.com", user.getEmail());
    }

    @Test
    public void testOrderAddressStorage() {
        OrderEntity order = new OrderEntity();
        String longAddress = "123 Main Street, Apt 4B, New York, NY 10001";
        order.setShippingAddress(longAddress);
        assertEquals(longAddress, order.getShippingAddress());
    }
}
