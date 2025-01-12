package com.project.yogerOrder.order.event;

import com.project.yogerOrder.order.entity.OrderEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderErroredEvent(@NotNull Long orderId, @NotBlank String eventId, @NotBlank OrderEventType eventType,
                                @NotNull OrderErroredData data, @NotNull LocalDateTime occurrenceTime) {

    private record OrderErroredData(@NotNull Long userId, @NotNull Long productId, @NotNull Integer orderQuantity) {
    }

    public static OrderErroredEvent from(OrderEntity orderEntity) {
        return new OrderErroredEvent(
                orderEntity.getId(),
                UUID.randomUUID().toString(),
                OrderEventType.ERROR,
                new OrderErroredData(orderEntity.getBuyerId(), orderEntity.getProductId(), orderEntity.getQuantity()),
                LocalDateTime.now()
        );
    }
}
