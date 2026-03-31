package com.example.springbootapp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CartItemTest {
    @Test
    public void cartItemGettersSetters() {
        CartItem item = new CartItem();
        
        item.setId(1L);
        assertEquals(1L, item.getId());
        
        Product product = new Product();
        product.setId(5L);
        item.setProduct(product);
        assertEquals(product, item.getProduct());
        
        Cart cart = new Cart();
        cart.setId(1L);
        item.setCart(cart);
        assertEquals(cart, item.getCart());
        
        item.setQuantity(5);
        assertEquals(5, item.getQuantity());
    }

    @Test
    public void cartItemEqualsAndHashCode() {
        CartItem item1 = new CartItem();
        item1.setId(1L);
        
        CartItem item2 = new CartItem();
        item2.setId(1L);
        
        CartItem item3 = new CartItem();
        item3.setId(2L);
        
        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
        assertNotEquals(item1, item3);
    }
}
