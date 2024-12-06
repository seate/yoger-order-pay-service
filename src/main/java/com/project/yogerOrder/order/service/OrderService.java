package com.project.yogerOrder.order.service;

import com.project.yogerOrder.order.config.OrderConfig;
import com.project.yogerOrder.order.dto.request.OrderRequestDTO;
import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.entity.OrderState;
import com.project.yogerOrder.order.exception.OrderNotFoundException;
import com.project.yogerOrder.order.exception.OrderRepositoryException;
import com.project.yogerOrder.order.repository.OrderRepository;
import com.project.yogerOrder.order.util.lock.service.OrderExpireLockService;
import com.project.yogerOrder.product.exception.ProductNotFoundException;
import com.project.yogerOrder.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final OrderConfig config;

    private final ProductService productService;

    private final OrderTransactionService orderTransactionService;

    private final OrderExpireLockService orderExpireLockService;


    // CREATE
    // 주문을 생성하는 과정을 먼저 진행하면, 주문이 생성되고, 재고 감소가 실패하기 전에 주문이 결제되면 문제가 생기기 때문에 재고 감소 먼저 진행
    // + 일반적으로 외부 서비스에 문제가 생기는 경우가 많기 때문에 외부 서비스 호출 먼저
    public Long orderProduct(Long userId, Long productId, OrderRequestDTO orderRequestDTO) {
        productService.decreaseStock(productId, orderRequestDTO.quantity());

        try {
            OrderEntity pendingOrder = OrderEntity.createPendingOrder(productId, orderRequestDTO.quantity(), userId);
            return orderRepository.save(pendingOrder).getId();
        } catch (Exception e) {
            // 보상 트랜잭션
            productService.increaseStock(productId, orderRequestDTO.quantity());
            throw new OrderRepositoryException();
        }
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
    public Integer countOrdersByProductId(Long productId) {
        return orderRepository.countAllByProductIdAndState(productId, OrderState.APPROVED);
    }

    // DELETE


    // UPDATE
    @Transactional
    public void updateStatusToPaidById(Long id) {
        findById(id).setState(OrderState.APPROVED);
    }

    // 주기적 pending 상태 order를 만료 상태로 변경하고 상품 재고 release
    @Scheduled(cron = "${order.cron.expiration}")
    public void orderExpirationSchedule() {
        // 다중 서버 환경에서 동시에 실행되는 것을 방지하기 위해 lock을 적용
        log.debug("Pending order expiration schedule trying to get lock");
        if (!orderExpireLockService.tryLock()) {
            log.debug("Pending order expiration schedule failed to get lock");
            return;
        }
        log.debug("Pending order expiration schedule got lock");

        try {
            orderRepository.findAllByState(OrderState.PENDING)
                    .parallelStream()
                    .filter(orderEntity -> !orderEntity.isPayable(config.validTime()))
                    .forEach(orderEntity -> {
                        // 외부 서비스 호출에 성공하면 DB를 업데이트하도록 변경
                        try {
                            productService.increaseStock(orderEntity.getProductId(), orderEntity.getQuantity());
                        } catch (ProductNotFoundException e) {
                            orderTransactionService.updateToErrorState(orderEntity);
                            log.error("Order(id={})'s product (id={}) is not present in productService, and exception message: {}",
                                    orderEntity.getId(), orderEntity.getProductId(), e.getMessage());
                            return;
                        }

                        try {
                            orderTransactionService.updateToExpiredState(orderEntity);
                        } catch (Exception e) {
                            // expired 상태로 변경하는데 실패하면 stock을 점유하기 위해서 다시 감소
                            log.error("Order(id={})'s state is not updated to expired, and exception message: {}",
                                    orderEntity.getId(), e.getMessage());
                            productService.decreaseStock(orderEntity.getProductId(), orderEntity.getQuantity());
                            throw new OrderRepositoryException();
                        }
                    });

            log.info("Pending order expiration schedule successfully executed");
        } finally {
            orderExpireLockService.unlock();
            log.debug("Pending order expiration schedule finished");
        }
    }
}
