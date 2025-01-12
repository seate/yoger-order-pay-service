package com.project.yogerOrder.order.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrdersCountRequestDTO(@NotEmpty List<Long> productIds) {
}
