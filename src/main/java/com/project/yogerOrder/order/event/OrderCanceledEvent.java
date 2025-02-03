package com.project.yogerOrder.order.event;

import com.project.yogerOrder.order.entity.OrderEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCanceledEvent(@NotNull Long orderId, @NotBlank String eventId, @NotNull OrderEventType eventType,
                                 @NotNull OrderCanceledData data, @NotNull LocalDateTime occurrenceTime) {

    private record OrderCanceledData(@NotNull Long userId, @NotNull Long productId, @NotNull Integer orderQuantity, @NotNull Boolean stockOccupied, @NotNull Boolean isPaymentCompleted) {
    }

    public static OrderCanceledEvent from(OrderEntity orderEntity, Boolean isStockOccupied, Boolean isPaymentCompleted) {
        return new OrderCanceledEvent(
                orderEntity.getId(),
                UUID.randomUUID().toString(),
                OrderEventType.CANCELED,
                new OrderCanceledData(
                        orderEntity.getBuyerId(),
                        orderEntity.getProductId(),
                        orderEntity.getQuantity(),
                        isStockOccupied,
                        isPaymentCompleted
                ),
                LocalDateTime.now()
        );
    }
}
