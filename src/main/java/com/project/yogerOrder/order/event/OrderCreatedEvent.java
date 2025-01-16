package com.project.yogerOrder.order.event;

import com.project.yogerOrder.order.entity.OrderEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCreatedEvent(@NotNull Long orderId, @NotBlank String eventId, @NotBlank OrderEventType eventType,
                                @NotNull OrderCreatedData data, @NotNull LocalDateTime occurrenceTime) {

    private record OrderCreatedData(@NotNull Long userId, @NotNull Long productId, @NotNull Integer orderQuantity) {
    }

    public static OrderCreatedEvent from(OrderEntity orderEntity) {
        return new OrderCreatedEvent(
                orderEntity.getId(),
                UUID.randomUUID().toString(),
                OrderEventType.CREATED,
                new OrderCreatedData(orderEntity.getBuyerId(), orderEntity.getProductId(), orderEntity.getQuantity()),
                LocalDateTime.now()
        );
    }
}
