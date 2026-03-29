package com.example.springbootapp.controller;

import com.example.springbootapp.model.OrderEntity;
import com.example.springbootapp.repository.OrderRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
public class StripeWebhookController {
    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookController.class);
    private final String webhookSecret;
    private final OrderRepository orderRepository;

    public StripeWebhookController(@Value("${stripe.webhook.secret:}") String webhookSecret, OrderRepository orderRepository) {
        this.webhookSecret = webhookSecret;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload, @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        try {
            Event event;
            if (webhookSecret != null && !webhookSecret.isBlank()) {
                event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            } else {
                // No webhook secret configured (dev); try to parse event without signature verification
                event = Event.GSON.fromJson(payload, Event.class);
            }

            String type = event.getType();
            logger.info("Received Stripe event: {}", type);
            if ("checkout.session.completed".equals(type)) {
                Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
                if (session != null) handleCheckoutCompleted(session);
            }
            return ResponseEntity.ok("");
        } catch (SignatureVerificationException ex) {
            logger.warn("Invalid webhook signature", ex);
            return ResponseEntity.status(400).body("Invalid signature");
        } catch (Exception ex) {
            logger.error("Webhook handling failed", ex);
            return ResponseEntity.status(500).body("Error");
        }
    }

    @Transactional
    public void handleCheckoutCompleted(Session session) {
        String sessionId = session.getId();
        Optional<OrderEntity> opt = orderRepository.findByPaymentSessionId(sessionId);
        if (opt.isPresent()) {
            OrderEntity order = opt.get();
            if (!"PAID".equals(order.getPaymentStatus())) {
                order.setPaymentStatus("PAID");
                orderRepository.save(order);
                logger.info("Order {} marked as PAID", order.getId());
            } else {
                logger.info("Order {} already PAID", order.getId());
            }
        } else {
            logger.warn("No order found for session id {}", sessionId);
        }
    }
}
