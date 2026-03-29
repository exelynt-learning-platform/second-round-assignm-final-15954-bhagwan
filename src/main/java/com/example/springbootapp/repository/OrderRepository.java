package com.example.springbootapp.repository;

import com.example.springbootapp.model.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
	java.util.Optional<OrderEntity> findByPaymentSessionId(String paymentSessionId);
}
