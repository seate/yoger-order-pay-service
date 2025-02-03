package com.project.yogerOrder.order.util.stateMachine;

import java.util.List;

public enum OrderState {
    CREATED, STOCK_CONFIRMED, PAYMENT_COMPLETED, COMPLETED, CANCELED, ERROR;

    public static List<OrderState> getPayableStates() {
        return List.of(CREATED, STOCK_CONFIRMED);
    }

    public static Boolean isStockOccupied(OrderState state) {
        return state == STOCK_CONFIRMED || state == COMPLETED;
    }

    public static Boolean isPaymentCompleted(OrderState state) {
        return state == PAYMENT_COMPLETED || state == COMPLETED;
    }

}
