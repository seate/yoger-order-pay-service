package com.project.yogerOrder.order.event;

import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.util.stateMachine.OrderState;
import com.project.yogerOrder.order.event.outbox.service.OrderOutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final OrderOutboxService orderOutboxService;

    public void sendEventByState(OrderEntity orderEntity) {
        Boolean isStockOccupied = OrderState.isStockOccupied(orderEntity.getState());
        Boolean isPaymentCompleted = OrderState.isPaymentCompleted(orderEntity.getState());

        if (orderEntity.getState() == OrderState.CREATED) {
            sendOrderCompletedEvent(orderEntity);
        } else if (orderEntity.getState() == OrderState.COMPLETED) {
            sendOrderCreatedEvent(orderEntity);
        } else if (orderEntity.getState() == OrderState.CANCELED) {
            sendOrderCanceledEvent(orderEntity, isStockOccupied, isPaymentCompleted);
        } else if (orderEntity.getState() == OrderState.ERROR) {
            sendOrderCanceledEvent(orderEntity, isStockOccupied, isPaymentCompleted);
            sendOrderErroredEvent(orderEntity);
        }
    }

    private void sendOrderCreatedEvent(OrderEntity orderEntity) {
        orderOutboxService.saveOutbox(orderEntity.getState().toString().toLowerCase(), OrderCreatedEvent.from(orderEntity));
    }

    private void sendOrderCompletedEvent(OrderEntity orderEntity) {
        orderOutboxService.saveOutbox(orderEntity.getState().toString().toLowerCase(), OrderCompletedEvent.from(orderEntity));
    }

    private void sendOrderCanceledEvent(OrderEntity orderEntity, Boolean isStockOccupied, Boolean isPaymentCompleted) {
        orderOutboxService.saveOutbox(orderEntity.getState().toString().toLowerCase(), OrderCanceledEvent.from(orderEntity, isStockOccupied, isPaymentCompleted));
    }

    private void sendOrderErroredEvent(OrderEntity orderEntity) {
        orderOutboxService.saveOutbox(orderEntity.getState().toString().toLowerCase(), OrderErroredEvent.from(orderEntity));
    }

    public void sendOrderDeductionAfterCanceledEvent(OrderEntity orderEntity) {
        orderOutboxService.saveOutbox(orderEntity.getState().toString().toLowerCase(), DeductionAfterOrderCanceledEvent.from(orderEntity));
    }

    public void sendPaymentCompleteAfterOrderCanceledEvent(OrderEntity orderEntity) {
        orderOutboxService.saveOutbox(orderEntity.getState().toString().toLowerCase(), PaymentCompletedAfterOrderCanceledEvent.from(orderEntity));
    }
}
