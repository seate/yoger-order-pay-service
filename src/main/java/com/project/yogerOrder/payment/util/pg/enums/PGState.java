package com.project.yogerOrder.payment.util.pg.enums;

public enum PGState {
    READY, PAID, FAILED;

    public Boolean isPaid() {
        return this == PAID;
    }

    public static PGState match(String status) {
        if (status.equals("ready")) {
            return READY;
        } else if (status.equals("paid")) {
            return PAID;
        } else {
            return FAILED;
        }
    }
}
