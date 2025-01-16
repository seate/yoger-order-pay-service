package com.project.yogerOrder.product.event;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ProductEventType {
    @JsonProperty("InventoryDeductionCompleted") INVENTORY_DEDUCTION_COMPLETED,
    @JsonProperty("InventoryDeductionFailed") INVENTORY_DEDUCTION_FAILED
}
