package com.project.yogerOrder.order.controller;

import com.project.yogerOrder.order.config.OrderTopic;
import com.project.yogerOrder.order.dto.request.OrderRequestDTO;
import com.project.yogerOrder.order.dto.request.OrdersCountRequestDTO;
import com.project.yogerOrder.order.dto.response.OrderCountResponseDTOs;
import com.project.yogerOrder.order.dto.response.OrderResponseDTOs;
import com.project.yogerOrder.order.dto.response.OrderResultResponseDTO;
import com.project.yogerOrder.order.service.OrderService;
import com.project.yogerOrder.payment.config.PaymentTopic;
import com.project.yogerOrder.payment.event.PaymentCanceledEvent;
import com.project.yogerOrder.payment.event.PaymentCompletedEvent;
import com.project.yogerOrder.product.config.ProductTopic;
import com.project.yogerOrder.product.event.ProductDeductionCompletedEvent;
import com.project.yogerOrder.product.event.ProductDeductionFailedEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/products/{productId}")
    public ResponseEntity<OrderResultResponseDTO> orderProduct(@RequestHeader("User-Id") Long userId,
                                                               @PathVariable("productId") Long productId,
                                                               @RequestBody @Valid OrderRequestDTO orderRequestDTO) {
        Long orderId = orderService.orderProduct(userId, productId, orderRequestDTO);

        return new ResponseEntity<>(new OrderResultResponseDTO(orderId), HttpStatus.CREATED);
    }

    @PostMapping("/products/count")
    public ResponseEntity<OrderCountResponseDTOs> countOrderByProductId(@RequestBody @Valid OrdersCountRequestDTO ordersCountRequestDTO) {
        return new ResponseEntity<>(orderService.countOrdersByProductIds(ordersCountRequestDTO), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<OrderResponseDTOs> findApprovedOrdersByUserId(@RequestHeader("User-Id") Long userId) {
        return new ResponseEntity<>(orderService.findApprovedOrdersByUserId(userId), HttpStatus.OK);
    }

    @KafkaListener(topics = ProductTopic.DEDUCTION_COMPLETED, groupId = "order-group",
            containerFactory = "productDeductionCompletedFactory")
    public void productDeductionCompleted(ProductDeductionCompletedEvent event) {
        orderService.updateByDeductionSuccess(event.data().orderId());
    }

    @KafkaListener(topics = ProductTopic.DEDUCTION_FAILED, groupId = "order-group",
            containerFactory = "productDeductionFailedFactory")
    public void productDeductionFailed(ProductDeductionFailedEvent event) {
        orderService.updateByDeductionFail(event.data().orderId());
    }

    @KafkaListener(topics = OrderTopic.COMPLETED, groupId = "order-group",
            containerFactory = "paymentCompletedFactory")
    public void paymentCompleted(PaymentCompletedEvent event) {
        orderService.updateByPaymentCompleted(event.data().orderId());
    }

    @KafkaListener(topics = PaymentTopic.CANCELED, groupId = "order-group",
            containerFactory = "paymentCanceledFactory")
    public void paymentCanceled(PaymentCanceledEvent event) {
        orderService.updateByPaymentCanceled(event.data().orderId());
    }

}

