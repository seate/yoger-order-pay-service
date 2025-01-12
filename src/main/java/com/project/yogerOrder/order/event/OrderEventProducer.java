package com.project.yogerOrder.order.event;

import com.project.yogerOrder.order.config.OrderTopic;
import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.entity.OrderState;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;


    public void sendOrderCreatedEvent(OrderEntity orderEntity) {
        kafkaTemplate.send(OrderTopic.CREATED, OrderCreatedEvent.from(orderEntity));
    }

    public void sendOrderCanceledEvent(OrderEntity orderEntity) {
        kafkaTemplate.send(OrderTopic.CANCELED, OrderCanceledEvent.from(orderEntity));
    }

    public void sendOrderCompletedEvent(OrderEntity orderEntity) {
        kafkaTemplate.send(OrderTopic.COMPLETED, OrderCompletedEvent.from(orderEntity));
    }

    public void sendOrderErroredEvent(OrderEntity orderEntity) {
        kafkaTemplate.send(OrderTopic.ERRORED, OrderErroredEvent.from(orderEntity));
    }

    public void sendEventByState(OrderEntity orderEntity) {
        if (orderEntity.getState() == OrderState.COMPLETED) {
            sendOrderCompletedEvent(orderEntity);
        } else if (orderEntity.getState() == OrderState.CREATED) {
            sendOrderCreatedEvent(orderEntity);
        } else if (orderEntity.getState() == OrderState.CANCELED) {
            sendOrderCanceledEvent(orderEntity);
        } else if (orderEntity.getState() == OrderState.ERROR) {
            sendOrderCanceledEvent(orderEntity);
            sendOrderErroredEvent(orderEntity);
        }
    }
}
