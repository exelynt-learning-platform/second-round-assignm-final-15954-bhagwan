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

    @Test
    public void createPaymentIncludesOrderId() {
        OrderRepository repo = mock(OrderRepository.class);
        PaymentService service = new PaymentService("", repo);
        OrderEntity o = new OrderEntity();
        o.setId(100L);
        o.setTotalPrice(50.00);
        String url = service.createPayment(o);
        assertTrue(url.contains("100"));
    }

    @Test
    public void createPaymentHandlesLargeAmounts() {
        OrderRepository repo = mock(OrderRepository.class);
        PaymentService service = new PaymentService("", repo);
        OrderEntity o = new OrderEntity();
        o.setId(1L);
        o.setTotalPrice(9999.99);
        String url = service.createPayment(o);
        assertNotNull(url);
        assertTrue(url.length() > 0);
    }

    @Test
    public void createPaymentHandlesZeroAmount() {
        OrderRepository repo = mock(OrderRepository.class);
        PaymentService service = new PaymentService("", repo);
        OrderEntity o = new OrderEntity();
        o.setId(2L);
        o.setTotalPrice(0.00);
        String url = service.createPayment(o);
        assertNotNull(url);
    }
}
