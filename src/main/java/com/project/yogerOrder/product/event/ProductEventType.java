package com.project.yogerOrder.product.event;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ProductEventType {
    @JsonProperty("deductionCompleted") DEDUCTION_COMPLETED,
    @JsonProperty("deductionFailed") DEDUCTION_FAILED
}
