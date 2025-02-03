package com.project.yogerOrder.order.event;

import com.project.yogerOrder.order.entity.OrderEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeductionAfterOrderCanceledEvent(@NotNull Long orderId, @NotBlank String eventId,
                                               @NotNull OrderDeductionAfterCanceledData data, @NotNull LocalDateTime occurrenceTime) {

    private record OrderDeductionAfterCanceledData(@NotNull Long userId, @NotNull Long productId, @NotNull Integer orderQuantity) {
    }

    public static DeductionAfterOrderCanceledEvent from(OrderEntity orderEntity) {
        return new DeductionAfterOrderCanceledEvent(
                orderEntity.getId(),
                UUID.randomUUID().toString(),
                new OrderDeductionAfterCanceledData(
                        orderEntity.getBuyerId(),
                        orderEntity.getProductId(),
                        orderEntity.getQuantity()
                ),
                LocalDateTime.now()
        );
    }
}
