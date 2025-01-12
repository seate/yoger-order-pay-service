package com.project.yogerOrder.payment.event;


import com.project.yogerOrder.payment.config.PaymentTopic;
import com.project.yogerOrder.payment.entity.PaymentEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

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
        kafkaTemplate.send(PaymentTopic.COMPLETED, PaymentCompletedEvent.from(paymentEntity));
    }

    private void sendPaymentCanceledEvent(PaymentEntity paymentEntity) {
        kafkaTemplate.send(PaymentTopic.CANCELED, PaymentCanceledEvent.from(paymentEntity));
    }

    private void sendPaymentErroredEvent(PaymentEntity paymentEntity) {
        kafkaTemplate.send(PaymentTopic.ERRORED, PaymentErroredEvent.from(paymentEntity));
    }

}
