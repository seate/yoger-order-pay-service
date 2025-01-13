package com.project.yogerOrder.order.event.outbox.service;

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

    public void saveOutbox(String eventType, Object payload) {
        orderOutboxRepository.save(new OrderOutboxEntity(eventType, payload));
    }
}
