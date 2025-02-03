package com.project.yogerOrder.global.util.stateMachine;

import lombok.Getter;

public class Transition<S, E> {

    private final S currentState;

    private final E event;

    @Getter
    private final S nextState;

    private final Runnable action;

    public Transition(S currentState, E event, S nextState, Runnable action) {
        this.currentState = currentState;
        this.event = event;
        this.nextState = nextState;
        this.action = action;
    }

    public void runAction() {
        if (action != null) {
            action.run();
        }
    }
}
