package com.project.yogerOrder.payment.event;


import com.project.yogerOrder.payment.entity.PaymentEntity;
import com.project.yogerOrder.payment.event.outbox.service.PaymentOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final PaymentOutboxService paymentOutboxService;

    public void sendEventByState(PaymentEntity paymentEntity) {
        switch (paymentEntity.getState()) {
            case PAID_END:
                sendPaymentCompletedEvent(paymentEntity);
                break;
            case TEMPORARY_PAID:
                //sendPaymentCompletedEvent(paymentEntity);
                log.info("Temporary paid payment is ignored: {}", paymentEntity);
                break;
            case CANCELED:
                sendPaymentCanceledEvent(paymentEntity);
                break;
            case ERROR:
                sendPaymentCanceledEvent(paymentEntity);
                sendPaymentErroredEvent(paymentEntity);
                break;
            default:
                throw new IllegalArgumentException("Unknown payment state: " + paymentEntity.getState());
        }
    }

    private void sendPaymentCompletedEvent(PaymentEntity paymentEntity) {
        paymentOutboxService.saveOutbox(paymentEntity.getState().toString(), PaymentCompletedEvent.from(paymentEntity));
    }

    private void sendPaymentCanceledEvent(PaymentEntity paymentEntity) {
        paymentOutboxService.saveOutbox(paymentEntity.getState().toString(), PaymentCanceledEvent.from(paymentEntity));
    }

    private void sendPaymentErroredEvent(PaymentEntity paymentEntity) {
        paymentOutboxService.saveOutbox(paymentEntity.getState().toString(), PaymentErroredEvent.from(paymentEntity));
    }

}
