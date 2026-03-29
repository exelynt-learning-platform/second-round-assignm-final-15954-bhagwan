package com.example.springbootapp.service;

import com.example.springbootapp.model.OrderEntity;
import com.example.springbootapp.repository.OrderRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class PaymentServiceTest {
    @Test
    public void createPaymentReturnsMockWhenNoKey() {
        OrderRepository repo = mock(OrderRepository.class);
        PaymentService service = new PaymentService("", repo);
        OrderEntity o = new OrderEntity();
        o.setId(42L);
        o.setTotalPrice(12.34);
        String url = service.createPayment(o);
        assertTrue(url.contains("/mock-pay?orderId=42"));
    }
}
