package com.project.yogerOrder.payment.event.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper;

    public void saveOutbox(String eventType, Object payload) {
        try {
            String stringPayload = objectMapper.writeValueAsString(payload);
            paymentOutboxRepository.save(new PaymentOutboxEntity(eventType, stringPayload));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
