package com.project.yogerOrder.payment.event;

import com.project.yogerOrder.payment.entity.PaymentEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentErroredEvent(@NotBlank String paymentId, @NotBlank String eventId, @NotBlank PaymentEventType eventType,
                                  @NotNull PaymentErroredData data, @NotNull LocalDateTime occurrenceTime) {

    public record PaymentErroredData(@NotNull Long userId, @NotNull Long orderId, @NotNull Integer totalPrice) {
    }

    public static PaymentErroredEvent from(PaymentEntity paymentEntity) {
        return new PaymentErroredEvent(
                paymentEntity.getPgPaymentId(),
                UUID.randomUUID().toString(),
                PaymentEventType.CANCELED,
                new PaymentErroredData(paymentEntity.getUserId(), paymentEntity.getOrderId(), paymentEntity.getAmount()),
                LocalDateTime.now()
        );
    }
}
