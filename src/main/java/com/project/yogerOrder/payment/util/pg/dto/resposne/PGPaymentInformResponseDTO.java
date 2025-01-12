package com.project.yogerOrder.payment.util.pg.dto.resposne;

import com.project.yogerOrder.payment.util.pg.enums.PGState;
import com.siot.IamportRestClient.response.Payment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PGPaymentInformResponseDTO(@NotBlank String pgPaymentId,
                                         @NotBlank String orderId,
                                         @NotNull Integer amount,
                                         @NotNull PGState status) {

    public static PGPaymentInformResponseDTO from(Payment payment) {
        return new PGPaymentInformResponseDTO(
                payment.getImpUid(),
                payment.getMerchantUid(),
                payment.getAmount().intValue(),
                PGState.match(payment.getStatus())
        );
    }

    public Boolean isPaid() {
        return this.status.isPaid();
    }
}
