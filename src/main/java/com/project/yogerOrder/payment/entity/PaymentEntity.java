package com.project.yogerOrder.payment.entity;

import com.project.yogerOrder.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = @Index(name = "idx_pg_payment_id", columnList = "pg_payment_id"))
public class PaymentEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, updatable = false)
    private String pgPaymentId;

    @NotNull
    @Column(nullable = false, unique = true, updatable = false)
    private Long orderId;

    @Min(1)
    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private Integer refundedAmount = 0;


    @Column(nullable = false, updatable = false)
    private Long userId;

    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private PaymentState state;

    private PaymentEntity(String pgPaymentId, Long orderId, Integer amount, Long userId, PaymentState state) {
        this.pgPaymentId = pgPaymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.userId = userId;
        this.state = state;
    }

    public static PaymentEntity createTempPaidPayment(String impUid, Long orderId, Integer amount, Long userId) {
        return new PaymentEntity(impUid, orderId, amount, userId, PaymentState.TEMPORARY_PAID);
    }

    public static PaymentEntity createCanceledPayment(String impUid, Long orderId, Integer amount, Long userId) {
        return new PaymentEntity(impUid, orderId, amount, userId, PaymentState.CANCELED);
    }

    public static PaymentEntity createErrorPayment(String impUid, Long orderId, Integer amount, Long userId) {
        return new PaymentEntity(impUid, orderId, amount, userId, PaymentState.ERROR);
    }

    public Boolean isPartialRefundable(Integer refundAmount) {
        return (refundAmount < this.amount) && (this.refundedAmount == 0) && (this.state == PaymentState.TEMPORARY_PAID);
    }

    public void partialRefund(Integer refundAmount) {
        if (!isPartialRefundable(refundAmount)) throw new IllegalStateException("This payment is not refundable");

        this.state = PaymentState.PAID_END;
        this.refundedAmount = refundAmount;
    }

    public boolean isPayCompletable() {
        return this.state == PaymentState.TEMPORARY_PAID;
    }

    public Boolean updateToCanceledState() {
        if (this.state == PaymentState.CANCELED) {
            return false;
        } else if (this.state != PaymentState.TEMPORARY_PAID && this.state != PaymentState.PAID_END) {
            this.state = PaymentState.CANCELED;

            return true;
        } else {
            throw new IllegalStateException("This payment is not cancelable");
        }
    }

    public Boolean updateToErrorState() {
        if (this.state == PaymentState.ERROR) return false;

        this.state = PaymentState.ERROR;

        return true;
    }
}
