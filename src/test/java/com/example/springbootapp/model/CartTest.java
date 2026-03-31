package com.example.springbootapp.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class CartTest {
    @Test
    public void cartGettersSetters() {
        Cart cart = new Cart();
        
        cart.setId(1L);
        assertEquals(1L, cart.getId());
        
        User user = new User();
        user.setId(1L);
        cart.setUser(user);
        assertEquals(user, cart.getUser());
        
        List<CartItem> items = new ArrayList<>();
        cart.setItems(items);
        assertEquals(items, cart.getItems());
    }
}
