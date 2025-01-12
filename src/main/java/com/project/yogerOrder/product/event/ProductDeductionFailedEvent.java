package com.project.yogerOrder.product.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ProductDeductionFailedEvent(@NotNull Long productId, @NotBlank String eventId, @NotNull ProductEventType eventType,
                                          @NotNull ProductDeductionFailedData data, @NotNull LocalDateTime occurrenceDateTime) {

    public record ProductDeductionFailedData(@NotNull Long orderId, @NotNull Integer orderQuantity) {
    }
}
