package com.project.yogerOrder.payment.event.outbox.repository;

import com.project.yogerOrder.payment.event.outbox.entity.PaymentOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentOutboxRepository extends JpaRepository<PaymentOutboxEntity, String> {
}
