package com.project.yogerOrder.payment.event.outbox.entity;

import com.project.yogerOrder.global.util.outbox.entity.OutboxEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class PaymentOutboxEntity extends OutboxEntity {

    public PaymentOutboxEntity(String eventType, String payload) {
        super(eventType, payload);
    }
}
