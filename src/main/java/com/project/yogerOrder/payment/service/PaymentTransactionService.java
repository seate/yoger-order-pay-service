package com.project.yogerOrder.payment.service;

import com.project.yogerOrder.payment.dto.request.ConfirmPaymentRequestDTO;
import com.project.yogerOrder.payment.entity.PaymentEntity;
import com.project.yogerOrder.payment.event.PaymentEventProducer;
import com.project.yogerOrder.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final PaymentRepository paymentRepository;

    private final PaymentEventProducer paymentEventProducer;

    @Transactional
    public void confirmPayment(ConfirmPaymentRequestDTO confirmPaymentRequestDTO) {
        PaymentEntity tempPayment = PaymentEntity.createTempPaidPayment(
                confirmPaymentRequestDTO.pgPaymentId(),
                confirmPaymentRequestDTO.orderId(),
                confirmPaymentRequestDTO.amount(),
                confirmPaymentRequestDTO.buyerId()
        );
        paymentRepository.save(tempPayment);

        paymentEventProducer.sendEventByState(tempPayment);
    }

    @Transactional
    public void refund(PaymentEntity paymentEntity, Integer refundAmount) {
        paymentEntity.partialRefund(refundAmount);
        paymentRepository.save(paymentEntity);

        paymentEventProducer.sendEventByState(paymentEntity);
    }
}
