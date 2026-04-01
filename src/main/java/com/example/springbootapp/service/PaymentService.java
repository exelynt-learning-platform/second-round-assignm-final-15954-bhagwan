package com.example.springbootapp.service;

import com.example.springbootapp.model.OrderEntity;
import com.example.springbootapp.model.PaymentStatus;
import com.example.springbootapp.repository.OrderRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private final String stripeKey;
    private final OrderRepository orderRepository;

    public PaymentService(@Value("${stripe.api.key:}") String stripeKey, OrderRepository orderRepository) {
        this.stripeKey = stripeKey;
        this.orderRepository = orderRepository;
        if (stripeKey != null && !stripeKey.isBlank()) Stripe.apiKey = stripeKey;
    }

    /**
     * Create a Stripe Checkout Session URL for payment.
     * Returns a URL the frontend can redirect to. In production handle webhooks.
     */
    public String createPayment(OrderEntity order) {
        if (stripeKey == null || stripeKey.isBlank()) {
            // fallback: set a mock session id and return a dummy URL for tests/dev
            order.setPaymentSessionId("mock-session-" + order.getId());
            order.setPaymentStatus(PaymentStatus.PENDING);
            orderRepository.save(order);
            return "/mock-pay?orderId=" + order.getId();
        }
        long amountCents = Math.round(order.getTotalPrice() * 100);
        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("usd")
                        .setUnitAmount(amountCents)
                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName("Order " + order.getId()).build())
                        .build())
                .setQuantity(1L)
                .build();
        SessionCreateParams params = SessionCreateParams.builder()
                .addLineItem(lineItem)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("https://example.com/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("https://example.com/cancel")
                .build();
        try {
            RequestOptions requestOptions = RequestOptions.builder()
                    .setIdempotencyKey("order-" + order.getId())
                    .build();
            Session session = Session.create(params, requestOptions);
            order.setPaymentSessionId(session.getId());
            order.setPaymentStatus(PaymentStatus.PENDING);
            orderRepository.save(order);
            return session.getUrl();
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }
}
