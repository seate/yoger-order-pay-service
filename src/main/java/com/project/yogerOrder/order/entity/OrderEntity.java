package com.project.yogerOrder.order.entity;

import com.project.yogerOrder.global.entity.BaseTimeEntity;
import com.project.yogerOrder.order.exception.IllegalOrderStateUpdateException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long productId;

    @Min(1)
    @NotNull
    private Integer quantity;

    @NotNull
    private Long buyerId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OrderState state;


    private OrderEntity(Long productId, Integer quantity, Long buyerId, OrderState state) {
        this.productId = productId;
        this.quantity = quantity;
        this.buyerId = buyerId;
        this.state = state;
    }

    public static OrderEntity createPendingOrder(Long productId, Integer quantity, Long buyerId) {
        return new OrderEntity(productId, quantity, buyerId, OrderState.CREATED);
    }

    public Boolean isPayable(Integer validTime) {
        return ((this.state == OrderState.CREATED) || (this.state == OrderState.STOCK_CONFIRMED))
                && getCreatedTime().isAfter(LocalDateTime.now().minusMinutes(validTime));
    }

    public Boolean stockConfirmed() {
        if (this.state == OrderState.STOCK_CONFIRMED || this.state == OrderState.COMPLETED) return false;
        else if (this.state == OrderState.CREATED) this.state = OrderState.STOCK_CONFIRMED;
        else if (this.state == OrderState.PAYMENT_COMPLETED) this.state = OrderState.COMPLETED;
        else this.state = OrderState.ERROR;

        return true;
    }

    public Boolean paymentCompleted() {
        if (this.state == OrderState.PAYMENT_COMPLETED || this.state == OrderState.COMPLETED) return false;
        else if (this.state == OrderState.CREATED) this.state = OrderState.PAYMENT_COMPLETED;
        else if (this.state == OrderState.STOCK_CONFIRMED) this.state = OrderState.COMPLETED;
        else this.state = OrderState.ERROR;

        return true;
    }

    public Boolean cancel() {
        if (this.state == OrderState.CANCELED) return false;
        else if (this.state != OrderState.CREATED && this.state != OrderState.STOCK_CONFIRMED && this.state != OrderState.PAYMENT_COMPLETED) {
            error();
            throw new IllegalOrderStateUpdateException();
        }
        this.state = OrderState.CANCELED;

        return true;
    }

    public Boolean error() {
        if (this.state == OrderState.ERROR) return false;
        else this.state = OrderState.ERROR;

        return true;
    }
}
