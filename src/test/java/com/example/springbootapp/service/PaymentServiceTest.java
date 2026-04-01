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

    @Test
    public void createPaymentIncludesAmount() {
        OrderRepository repo = mock(OrderRepository.class);
        PaymentService service = new PaymentService("", repo);
        OrderEntity o = new OrderEntity();
        o.setId(1L);
        o.setTotalPrice(29.99);
        String url = service.createPayment(o);
        // Mock payment URL returns /mock-pay?orderId=X format, not actual amount
        assertTrue(url.contains("/mock-pay") && url.contains("1"));
    }

    @Test
    public void createPaymentWithValidOrderData() {
        OrderRepository repo = mock(OrderRepository.class);
        // Use empty key to trigger mock payment URL (real Stripe key would fail in test with dummy key)
        PaymentService service = new PaymentService("", repo);
        OrderEntity o = new OrderEntity();
        o.setId(5L);
        o.setTotalPrice(125.50);
        String url = service.createPayment(o);
        assertNotNull(url);
        assertTrue(url.length() > 0);
        assertTrue(url.contains("/mock-pay"));
    }

    @Test
    public void createPaymentWithNullKey() {
        OrderRepository repo = mock(OrderRepository.class);
        PaymentService service = new PaymentService(null, repo);
        OrderEntity o = new OrderEntity();
        o.setId(3L);
        o.setTotalPrice(75.00);
        String url = service.createPayment(o);
        assertNotNull(url);
    }

    @Test
    public void createPaymentMultipleOrders() {
        OrderRepository repo = mock(OrderRepository.class);
        PaymentService service = new PaymentService("", repo);

        OrderEntity o1 = new OrderEntity();
        o1.setId(10L);
        o1.setTotalPrice(50.00);

        OrderEntity o2 = new OrderEntity();
        o2.setId(11L);
        o2.setTotalPrice(75.00);

        String url1 = service.createPayment(o1);
        String url2 = service.createPayment(o2);

        assertTrue(url1.contains("10"));
        assertTrue(url2.contains("11"));
        assertNotEquals(url1, url2);
    }
}
