package com.project.yogerOrder.product.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ProductDeductionCompletedEvent(@NotNull Long productId, @NotBlank String eventId, @NotNull ProductEventType eventType,
                                             @NotNull ProductDeductionCompletedData data, @NotNull LocalDateTime occurrenceDateTime) {

    public record ProductDeductionCompletedData(@NotNull Long orderId, @NotNull Integer orderQuantity) {
    }
}
