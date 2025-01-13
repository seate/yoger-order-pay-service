package com.project.yogerOrder.payment.event.outbox.service;

import com.project.yogerOrder.payment.event.outbox.entity.PaymentOutboxEntity;
import com.project.yogerOrder.payment.event.outbox.repository.PaymentOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentOutboxService {

    private final PaymentOutboxRepository paymentOutboxRepository;

    public void saveOutbox(String eventType, Object payload) {
        paymentOutboxRepository.save(new PaymentOutboxEntity(eventType, payload));
    }
}
