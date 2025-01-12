package com.project.yogerOrder.product.dto.response;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PriceByQuantity(@NotNull @Min(0) Integer quantity, @NotNull @Min(0) Integer price) {
}