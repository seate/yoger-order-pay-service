package com.project.yogerOrder.order.repository;

import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.entity.OrderState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findAllByState(OrderState state);

    Integer countAllByProductIdAndState(Long productId, OrderState orderState);

    List<OrderEntity> findAllByBuyerIdAndState(Long buyerId, OrderState state);
}
