package com.project.yogerOrder.order.event;

import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.entity.OrderState;
import com.project.yogerOrder.order.event.outbox.service.OrderOutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final OrderOutboxService orderOutboxService;


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

    private void sendOrderCreatedEvent(OrderEntity orderEntity) {
        orderOutboxService.saveOutbox(orderEntity.getState().toString(), OrderCreatedEvent.from(orderEntity));
    }

    private void sendOrderCanceledEvent(OrderEntity orderEntity) {
        orderOutboxService.saveOutbox(orderEntity.getState().toString(), OrderCanceledEvent.from(orderEntity));
    }

    private void sendOrderCompletedEvent(OrderEntity orderEntity) {
        orderOutboxService.saveOutbox(orderEntity.getState().toString(), OrderCompletedEvent.from(orderEntity));
    }

    private void sendOrderErroredEvent(OrderEntity orderEntity) {
        orderOutboxService.saveOutbox(orderEntity.getState().toString(), OrderErroredEvent.from(orderEntity));
    }
}
