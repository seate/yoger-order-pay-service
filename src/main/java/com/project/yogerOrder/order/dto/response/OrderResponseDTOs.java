package com.project.yogerOrder.order.dto.response;

import com.project.yogerOrder.order.entity.OrderEntity;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderResponseDTOs(@NotNull List<OrderResponseDTO> orderResponseDTOs) {

    public static OrderResponseDTOs from(List<OrderEntity> orderEntities) {
        return new OrderResponseDTOs(orderEntities.stream().map(OrderResponseDTO::from).toList());
    }
}
