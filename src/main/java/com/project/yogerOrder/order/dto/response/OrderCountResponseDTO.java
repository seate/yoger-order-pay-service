package com.project.yogerOrder.order.dto.response;

import jakarta.validation.constraints.NotNull;

public record OrderCountResponseDTO(@NotNull Long productId, @NotNull Integer count) {
}
