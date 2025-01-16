package com.project.yogerOrder.order.event.outbox.entity;

import com.project.yogerOrder.global.util.outbox.entity.OutboxEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class OrderOutboxEntity extends OutboxEntity {

    public OrderOutboxEntity(String eventType, String payload) {
        super(eventType, payload);
    }
}
