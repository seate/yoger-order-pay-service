package com.project.yogerOrder.order.event.outbox.repository;

import com.project.yogerOrder.order.event.outbox.entity.OrderOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderOutboxRepository extends JpaRepository<OrderOutboxEntity, String> {
}
