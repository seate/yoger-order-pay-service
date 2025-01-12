package com.project.yogerOrder.product.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductResponseDTO(@NotNull Long id,
                                 @NotEmpty List<PriceByQuantity> priceByQuantities) {

    public Integer originalMaxPrice() {
        return priceByQuantities.stream()
                .map(PriceByQuantity::price)
                .max(Integer::compareTo)
                .orElseThrow(() -> new IllegalArgumentException("가격 정보가 존재하지 않습니다.")); //TODO 변경
    }
}
