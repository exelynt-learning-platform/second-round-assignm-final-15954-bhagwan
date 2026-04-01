package com.example.springbootapp.controller;

import com.example.springbootapp.model.OrderEntity;
import com.example.springbootapp.model.PaymentStatus;
import com.example.springbootapp.repository.OrderRepository;
import com.example.springbootapp.service.OrderService;
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
    private final OrderService orderService;

    public StripeWebhookController(@Value("${stripe.webhook.secret:}") String webhookSecret, 
                                   OrderRepository orderRepository, OrderService orderService) {
        this.webhookSecret = webhookSecret;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload, @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        try {
            // Require signature verification
            if (webhookSecret == null || webhookSecret.isBlank()) {
                logger.error("Webhook secret not configured - rejecting unsigned webhook");
                return ResponseEntity.status(401).body("Webhook secret not configured");
            }
            if (sigHeader == null || sigHeader.isBlank()) {
                logger.warn("Missing Stripe-Signature header");
                return ResponseEntity.status(400).body("Missing signature header");
            }
            
            logger.debug("Processing webhook payload with signature verification");
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            String type = event.getType();
            logger.info("Received Stripe event: {}", type);
            
            if ("checkout.session.completed".equals(type)) {
                Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
                if (session != null) {
                    logger.debug("Processing checkout session: {}", session.getId());
                    handleCheckoutCompleted(session);
                } else {
                    logger.warn("Received checkout.session.completed event but session is null");
                }
            } else {
                logger.debug("Ignoring unhandled event type: {}", type);
            }
            return ResponseEntity.ok("");
        } catch (SignatureVerificationException ex) {
            logger.warn("Invalid webhook signature: {}", ex.getMessage());
            return ResponseEntity.status(400).body("Invalid signature");
        } catch (Exception ex) {
            logger.error("Webhook handling failed with error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body("Error");
        }
    }

    @Transactional
    public void handleCheckoutCompleted(Session session) {
        String sessionId = session.getId();
        Optional<OrderEntity> opt = orderRepository.findByPaymentSessionId(sessionId);
        if (opt.isPresent()) {
            OrderEntity order = opt.get();
            if (order.getPaymentStatus() != PaymentStatus.PAID) {
                // Mark order as paid
                order.setPaymentStatus(PaymentStatus.PAID);
                orderRepository.save(order);
                logger.info("Order {} marked as PAID", order.getId());
                
                // Update stock quantities now that payment is confirmed
                // This prevents race condition where stock was updated before payment
                try {
                    orderService.updateStockForConfirmedOrder(order.getId());
                    logger.info("Stock updated for Order {}", order.getId());
                } catch (Exception e) {
                    logger.error("Failed to update stock for Order {}: {}", order.getId(), e.getMessage(), e);
                    // Note: Order is already marked as PAID, but stock update failed
                    // This should trigger a manual review or retry mechanism in production
                }
            } else {
                logger.info("Order {} already PAID", order.getId());
            }
        } else {
            logger.warn("No order found for session id {}", sessionId);
        }
    }
}
