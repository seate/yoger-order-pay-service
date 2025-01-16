package com.project.yogerOrder.order.dto.response;

import com.project.yogerOrder.order.entity.OrderEntity;
import jakarta.validation.constraints.NotNull;

public record OrderResponseDTO(@NotNull String orderId, @NotNull Long productId, @NotNull Integer quantity) {

    public static OrderResponseDTO from(OrderEntity orderEntity) {
        return new OrderResponseDTO(
                orderEntity.getId().toString(),
                orderEntity.getProductId(),
                orderEntity.getQuantity()
        );
    }
}
