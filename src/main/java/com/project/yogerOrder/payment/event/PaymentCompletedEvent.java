package com.project.yogerOrder.payment.event;

import com.project.yogerOrder.payment.entity.PaymentEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentCompletedEvent(@NotNull String paymentId, @NotBlank String eventId, @NotBlank PaymentEventType eventType,
                                    @NotNull PaymentCompletedData data, @NotNull LocalDateTime occurrenceTime) {

    public record PaymentCompletedData(@NotNull Long userId, @NotNull Long orderId, @NotNull Integer totalPrice) {
    }

    public static PaymentCompletedEvent from(PaymentEntity paymentEntity) {
        return new PaymentCompletedEvent(
                paymentEntity.getPgPaymentId(),
                UUID.randomUUID().toString(),
                PaymentEventType.COMPLETED,
                new PaymentCompletedData(paymentEntity.getUserId(), paymentEntity.getOrderId(), paymentEntity.getAmount()),
                LocalDateTime.now()
        );
    }
}
