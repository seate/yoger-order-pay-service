package com.project.yogerOrder.order.exception;

import com.project.yogerOrder.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class IllegalOrderStateUpdateException extends BusinessException {

    public IllegalOrderStateUpdateException() {
        super(HttpStatus.CONFLICT, "유효하지 않은 주문상태 변경입니다.");
    }
}
