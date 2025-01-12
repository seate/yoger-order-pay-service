package com.project.yogerOrder.order.event;

import com.project.yogerOrder.order.entity.OrderEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCompletedEvent(@NotNull Long orderId, @NotBlank String eventId, @NotBlank OrderEventType eventType,
                                  @NotNull OrderCompletedData data, @NotNull LocalDateTime occurrenceTime) {

    private record OrderCompletedData(@NotNull Long userId, @NotNull Long productId, @NotNull Integer orderQuantity) {
    }

    public static OrderCompletedEvent from(OrderEntity orderEntity) {
        return new OrderCompletedEvent(
                orderEntity.getId(),
                UUID.randomUUID().toString(),
                OrderEventType.COMPLETED,
                new OrderCompletedData(orderEntity.getBuyerId(), orderEntity.getProductId(), orderEntity.getQuantity()),
                LocalDateTime.now()
        );
    }

}
