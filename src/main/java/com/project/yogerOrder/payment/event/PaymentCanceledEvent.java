package com.project.yogerOrder.payment.event;

import com.project.yogerOrder.payment.entity.PaymentEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentCanceledEvent(@NotBlank String paymentId, @NotBlank String eventId, @NotBlank PaymentEventType eventType,
                                   @NotNull PaymentCanceledEvent.PaymentCanceledData data, @NotNull LocalDateTime occurrenceTime) {

    public record PaymentCanceledData(@NotNull Long userId, @NotNull Long orderId, @NotNull Integer totalPrice) {
    }

    public static PaymentCanceledEvent from(PaymentEntity paymentEntity) {
        return new PaymentCanceledEvent(
                paymentEntity.getPgPaymentId(),
                UUID.randomUUID().toString(),
                PaymentEventType.CANCELED,
                new PaymentCanceledData(paymentEntity.getUserId(), paymentEntity.getOrderId(), paymentEntity.getAmount()),
                LocalDateTime.now()
        );
    }
}
