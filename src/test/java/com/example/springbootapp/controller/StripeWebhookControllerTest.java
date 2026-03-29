package com.example.springbootapp.controller;

import com.example.springbootapp.model.OrderEntity;
import com.example.springbootapp.repository.OrderRepository;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StripeWebhookControllerTest {
    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    StripeWebhookController controller;

    @Test
    public void handleCheckoutCompleted_marksPaidOnce() {
        OrderEntity order = new OrderEntity();
        order.setId(77L);
        order.setPaymentSessionId("sess_123");
        order.setPaymentStatus("PENDING");
        when(orderRepository.findByPaymentSessionId("sess_123")).thenReturn(Optional.of(order));

        Session s = new Session();
        s.setId("sess_123");

        controller.handleCheckoutCompleted(s);
        // saved once
        verify(orderRepository, times(1)).save(order);

        // mark as PAID
        assert("PAID".equals(order.getPaymentStatus()));

        // calling again should not save again
        controller.handleCheckoutCompleted(s);
        verify(orderRepository, times(1)).save(order);
    }
}
