package com.project.yogerOrder.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PartialRefundRequestDTO(@NotNull Long productId,
                                      @NotNull @Min(0) Integer originalMaxPrice,
                                      @NotNull @Min(0) Integer confirmedPrice) {
}
