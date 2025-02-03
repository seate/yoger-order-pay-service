package com.project.yogerOrder.order.util.stateMachine;

import com.project.yogerOrder.global.util.stateMachine.Transition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OrderStaticStateMachine {

    private static final Map<OrderState, Map<OrderStateChangeEvent, Transition<OrderState, OrderStateChangeEvent>>> transitions = new ConcurrentHashMap<>();

    private static final OrderState errorState = OrderState.ERROR;
    
    static {
        entryTransition(OrderState.CREATED, OrderStateChangeEvent.STOCK_DEDUCTED, OrderState.STOCK_CONFIRMED);
        entryTransition(OrderState.CREATED, OrderStateChangeEvent.PAID, OrderState.PAYMENT_COMPLETED);
        entryTransition(OrderState.CREATED, OrderStateChangeEvent.CANCELED, OrderState.CANCELED);

        entryTransition(OrderState.STOCK_CONFIRMED, OrderStateChangeEvent.PAID, OrderState.COMPLETED);
        entryTransition(OrderState.STOCK_CONFIRMED, OrderStateChangeEvent.CANCELED, OrderState.CANCELED);
        entryTransition(OrderState.STOCK_CONFIRMED, OrderStateChangeEvent.STOCK_DEDUCTED, OrderState.STOCK_CONFIRMED);

        entryTransition(OrderState.PAYMENT_COMPLETED, OrderStateChangeEvent.STOCK_DEDUCTED, OrderState.COMPLETED);
        entryTransition(OrderState.PAYMENT_COMPLETED, OrderStateChangeEvent.CANCELED, OrderState.CANCELED);
        entryTransition(OrderState.PAYMENT_COMPLETED, OrderStateChangeEvent.PAID, OrderState.PAYMENT_COMPLETED);

        entryTransition(OrderState.COMPLETED, OrderStateChangeEvent.STOCK_DEDUCTED, OrderState.COMPLETED);
        entryTransition(OrderState.COMPLETED, OrderStateChangeEvent.PAID, OrderState.COMPLETED);
        entryTransition(OrderState.COMPLETED, OrderStateChangeEvent.CANCELED, OrderState.CANCELED);

        entryTransition(OrderState.CANCELED, OrderStateChangeEvent.STOCK_DEDUCTED, OrderState.CANCELED);
        entryTransition(OrderState.CANCELED, OrderStateChangeEvent.PAID, OrderState.CANCELED);
        entryTransition(OrderState.CANCELED, OrderStateChangeEvent.CANCELED, OrderState.CANCELED);
    }

    private static void entryTransition(OrderState currentState, OrderStateChangeEvent event, OrderState nextState) {
        entryTransition(currentState, event, nextState, null);
    }

    private static void entryTransition(OrderState currentState, OrderStateChangeEvent event, OrderState nextState, Runnable action) {
        transitions.putIfAbsent(currentState, new ConcurrentHashMap<>());
        transitions.get(currentState).put(event, new Transition<>(currentState, event, nextState, action));
    }

    public static OrderState nextState(OrderState currentState, OrderStateChangeEvent event) {
        Transition<OrderState, OrderStateChangeEvent> transition = transitions.get(currentState).get(event);
        if (transition == null) return errorState;

        transition.runAction();

        return transition.getNextState();
    }
}
