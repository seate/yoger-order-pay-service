package com.project.yogerOrder.order.event.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.yogerOrder.order.event.outbox.entity.OrderOutboxEntity;
import com.project.yogerOrder.order.event.outbox.repository.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderOutboxService {

    private final OrderOutboxRepository orderOutboxRepository;

    private final ObjectMapper objectMapper;

    public void saveOutbox(String eventType, Object payload) {
        try {
            String stringPayload = objectMapper.writeValueAsString(payload);
            orderOutboxRepository.save(new OrderOutboxEntity(eventType, stringPayload));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
