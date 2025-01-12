package com.project.yogerOrder.order.entity;

import java.util.List;

public enum OrderState {
    CREATED, STOCK_CONFIRMED, PAYMENT_COMPLETED, COMPLETED, CANCELED, ERROR;

    public static List<OrderState> getPayableStates() {
        return List.of(CREATED, STOCK_CONFIRMED);
    }
}
