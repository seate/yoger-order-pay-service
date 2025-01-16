package com.project.yogerOrder.order.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderResultResponseDTO(@NotBlank String orderId) {

    public OrderResultResponseDTO(@NotNull Long orderId) {
        this(String.valueOf(orderId));
    }
}
