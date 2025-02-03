package com.project.yogerOrder.order.entity;

import com.project.yogerOrder.global.entity.BaseTimeEntity;
import com.project.yogerOrder.order.util.stateMachine.OrderState;
import com.project.yogerOrder.order.util.stateMachine.OrderStateChangeEvent;
import com.project.yogerOrder.order.util.stateMachine.OrderStaticStateMachine;
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

    public Boolean changeState(OrderStateChangeEvent orderStateChangeEvent) {
        OrderState nextState = OrderStaticStateMachine.nextState(this.state, orderStateChangeEvent);
        boolean isUpdated = (this.state != nextState);
        this.state = nextState;

        return isUpdated;
    }
}
