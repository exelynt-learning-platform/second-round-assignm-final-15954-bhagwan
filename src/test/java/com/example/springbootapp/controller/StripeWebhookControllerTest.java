package com.example.springbootapp.controller;

import com.example.springbootapp.model.OrderEntity;
import com.example.springbootapp.model.PaymentStatus;
import com.example.springbootapp.repository.OrderRepository;
import com.example.springbootapp.service.OrderService;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StripeWebhookControllerTest {
    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderService orderService;

    @InjectMocks
    StripeWebhookController controller;

    @Test
    public void handleCheckoutCompleted_marksPaidOnce() {
        OrderEntity order = new OrderEntity();
        order.setId(77L);
        order.setPaymentSessionId("sess_123");
        order.setPaymentStatus(PaymentStatus.PENDING);
        when(orderRepository.findByPaymentSessionId("sess_123")).thenReturn(Optional.of(order));

        Session s = new Session();
        s.setId("sess_123");

        controller.handleCheckoutCompleted(s);
        verify(orderRepository, times(1)).save(order);
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());

        controller.handleCheckoutCompleted(s);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    public void handleCheckoutCompleted_doesNothingIfOrderNotFound() {
        when(orderRepository.findByPaymentSessionId("sess_nonexistent")).thenReturn(Optional.empty());

        Session s = new Session();
        s.setId("sess_nonexistent");

        controller.handleCheckoutCompleted(s);
        verify(orderRepository, never()).save(any());
    }

    @Test
    public void handleCheckoutCompleted_doesNothingIfAlreadyPaid() {
        OrderEntity order = new OrderEntity();
        order.setId(78L);
        order.setPaymentSessionId("sess_456");
        order.setPaymentStatus(PaymentStatus.PAID);
        when(orderRepository.findByPaymentSessionId("sess_456")).thenReturn(Optional.of(order));

        Session s = new Session();
        s.setId("sess_456");

        controller.handleCheckoutCompleted(s);
        verify(orderRepository, never()).save(any());
    }

    @Test
    public void handleCheckoutCompleted_callsUpdateStockForConfirmedOrder() {
        OrderEntity order = new OrderEntity();
        order.setId(79L);
        order.setPaymentSessionId("sess_789");
        order.setPaymentStatus(PaymentStatus.PENDING);
        when(orderRepository.findByPaymentSessionId("sess_789")).thenReturn(Optional.of(order));

        Session s = new Session();
        s.setId("sess_789");

        controller.handleCheckoutCompleted(s);
        verify(orderRepository, times(1)).save(order);
        verify(orderService, times(1)).updateStockForConfirmedOrder(79L);
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
    }

    @Test
    public void handleCheckoutCompleted_handlesStockUpdateError() {
        OrderEntity order = new OrderEntity();
        order.setId(80L);
        order.setPaymentSessionId("sess_fail");
        order.setPaymentStatus(PaymentStatus.PENDING);
        when(orderRepository.findByPaymentSessionId("sess_fail")).thenReturn(Optional.of(order));
        doThrow(new RuntimeException("Stock update error")).when(orderService).updateStockForConfirmedOrder(80L);

        Session s = new Session();
        s.setId("sess_fail");

        controller.handleCheckoutCompleted(s);
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    public void handleWebhook_missingSignatureSecret() {
        ReflectionTestUtils.setField(controller, "webhookSecret", "");

        var resp = controller.handleWebhook("payload", "sig");
        assertEquals(401, resp.getStatusCode().value());
    }

    @Test
    public void handleWebhook_missingSigHeader() {
        ReflectionTestUtils.setField(controller, "webhookSecret", "secret");

        var resp = controller.handleWebhook("payload", "");
        assertEquals(400, resp.getStatusCode().value());
    }

    @Test
    public void handleWebhook_nullSigHeader() {
        ReflectionTestUtils.setField(controller, "webhookSecret", "secret");

        var resp = controller.handleWebhook("payload", null);
        assertEquals(400, resp.getStatusCode().value());
    }
}

