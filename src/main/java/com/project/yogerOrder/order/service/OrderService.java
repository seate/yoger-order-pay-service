package com.project.yogerOrder.order.service;

import com.project.yogerOrder.order.config.OrderConfig;
import com.project.yogerOrder.order.dto.request.OrderRequestDTO;
import com.project.yogerOrder.order.dto.request.OrdersCountRequestDTO;
import com.project.yogerOrder.order.dto.response.OrderCountResponseDTO;
import com.project.yogerOrder.order.dto.response.OrderCountResponseDTOs;
import com.project.yogerOrder.order.dto.response.OrderResponseDTOs;
import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.entity.OrderState;
import com.project.yogerOrder.order.event.OrderEventProducer;
import com.project.yogerOrder.order.exception.OrderNotFoundException;
import com.project.yogerOrder.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final OrderConfig config;

    private final OrderEventProducer orderEventProducer;


    // CREATE
    // 주문을 생성하는 과정을 먼저 진행하면, 주문이 생성되고, 재고 감소가 실패하기 전에 주문이 결제되면 문제가 생기기 때문에 재고 감소 먼저 진행
    // + 일반적으로 외부 서비스에 문제가 생기는 경우가 많기 때문에 외부 서비스 호출 먼저
    public Long orderProduct(Long userId, Long productId, OrderRequestDTO orderRequestDTO) {
        OrderEntity pendingOrder = OrderEntity.createPendingOrder(productId, orderRequestDTO.quantity(), userId);
        OrderEntity orderEntity = orderRepository.save(pendingOrder);

        orderEventProducer.sendEventByState(orderEntity);

        return orderEntity.getId();
    }


    // READ
    @Transactional(readOnly = true)
    public OrderEntity findById(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(OrderNotFoundException::new);
    }

    public Boolean isPayable(OrderEntity orderEntity) {
        return orderEntity.isPayable(config.validTime());
    }

    @Transactional(readOnly = true)
    public OrderCountResponseDTOs countOrdersByProductIds(OrdersCountRequestDTO ordersCountRequestDTO) {
        List<OrderCountResponseDTO> orderCountResponseDTOS = ordersCountRequestDTO.productIds().stream().map(productId ->
                        new OrderCountResponseDTO(
                                productId,
                                orderRepository.countAllByProductIdAndState(productId, OrderState.COMPLETED)
                        )
                ).toList();

        return new OrderCountResponseDTOs(orderCountResponseDTOS);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTOs findApprovedOrdersByUserId(Long userId) {
        return OrderResponseDTOs.from(orderRepository.findAllByBuyerIdAndState(userId, OrderState.COMPLETED));
    }

    // UPDATE
    @Transactional
    public void updateByDeductionSuccess(Long orderId) {
        OrderEntity orderEntity = findById(orderId);

        if (orderEntity.getState() == OrderState.CANCELED) {
            orderEventProducer.sendOrderDeductionAfterCanceledEvent(orderEntity);
            return;
        }

        Boolean isUpdated = orderEntity.stockConfirmed();
        if (!isUpdated) {
            log.debug("Order is already completed. orderId: {}", orderEntity.getId());
            return;
        }
        orderRepository.save(orderEntity);

        orderEventProducer.sendEventByState(orderEntity);
    }

    @Transactional
    public void updateByDeductionFail(Long orderId) {
        OrderEntity orderEntity = findById(orderId);
        Boolean isUpdated = orderEntity.cancel();
        if (!isUpdated) {
            log.debug("Order is already canceled. orderId: {}", orderEntity.getId());
            return;
        }
        orderRepository.save(orderEntity);

        orderEventProducer.sendEventByState(orderEntity);
    }

    @Transactional
    public void updateByPaymentCompleted(Long id) {
        OrderEntity orderEntity = findById(id);
        Boolean isUpdated = orderEntity.paymentCompleted();
        if (!isUpdated) {
            log.debug("Order is already completed. orderId: {}", orderEntity.getId());
            return;
        }
        orderRepository.save(orderEntity);

        orderEventProducer.sendEventByState(orderEntity);
    }

    @Transactional
    public void updateByPaymentCanceled(Long id) {
        OrderEntity orderEntity = findById(id);
        Boolean isUpdated = orderEntity.cancel();
        if (!isUpdated) {
            log.debug("Order is already canceled. orderId: {}", orderEntity.getId());
            return;
        }
        orderRepository.save(orderEntity);

        orderEventProducer.sendEventByState(orderEntity);
    }

    @Transactional
    public void updateByExpiration(OrderEntity orderEntity) {
        Boolean isUpdated = orderEntity.cancel();
        if (!isUpdated) {
            log.debug("Order is already canceled. orderId: {}", orderEntity.getId());
            return;
        }
        orderRepository.save(orderEntity);

        orderEventProducer.sendEventByState(orderEntity);
    }


    // 주기적 pending 상태 order를 만료 상태로 변경하고 상품 재고 release
    @Scheduled(cron = "${order.cron.expiration}")
    @SchedulerLock(name = "orderExpirationSchedule", lockAtMostFor = "PT50S", lockAtLeastFor = "PT40S")
    public void orderExpirationSchedule() {
        OrderState.getPayableStates().forEach(orderState ->
                orderRepository.findAllByState(orderState)
                        .parallelStream()
                        .filter(orderEntity -> !orderEntity.isPayable(config.validTime()))
                        .forEach(this::updateByExpiration)
        );

        log.info("Pending order expiration schedule successfully executed");
    }
}
