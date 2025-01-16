package com.project.yogerOrder.order.service;

import com.project.yogerOrder.order.config.OrderConfig;
import com.project.yogerOrder.order.dto.request.OrderRequestDTO;
import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.entity.OrderState;
import com.project.yogerOrder.order.exception.OrderRepositoryException;
import com.project.yogerOrder.order.repository.OrderRepository;
import com.project.yogerOrder.product.service.ProductService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.stream.Stream;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(OrderConfig.class)
class OrderServiceTest {

    @Mock
    OrderConfig orderConfig;

    @Mock
    OrderRepository orderRepository;

    @Mock
    ProductService productService;

    @InjectMocks
    OrderService orderService;


    Long orderId = 12323123L;
    Long productId = 1L;
    Integer quantity = 1;
    Long userId = 1L;
    OrderRequestDTO orderRequestDTO = new OrderRequestDTO(quantity);

    @Test
    void orderProductSuccess() {
        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);

        // given
        OrderEntity pendingOrder = OrderEntity.createPendingOrder(productId, quantity, userId);
        ReflectionTestUtils.setField(pendingOrder, "id", orderId);

        // when
        Mockito.when(orderRepository.save(orderCaptor.capture())).thenReturn(pendingOrder);
        Long id = orderService.orderProduct(userId, productId, orderRequestDTO);

        // then
        Mockito.verify(productService).decreaseStock(productId, orderRequestDTO.quantity());

        Assertions.assertThat(id).isEqualTo(orderId);
        Assertions.assertThat(orderCaptor.getValue().getProductId()).isEqualTo(productId);
        Assertions.assertThat(orderCaptor.getValue().getQuantity()).isEqualTo(quantity);
        Assertions.assertThat(orderCaptor.getValue().getBuyerId()).isEqualTo(userId);
    }

    @Test
    void orderProduct_RollbackOnFailure() {
        // given
        Mockito.doNothing().when(productService).decreaseStock(productId, orderRequestDTO.quantity());
        Mockito.when(orderRepository.save(Mockito.any(OrderEntity.class)))
                .thenThrow(new RuntimeException()); // 주문 저장 시 예외 발생

        // when, then
        Assertions.assertThatThrownBy(() -> orderService.orderProduct(userId, productId, orderRequestDTO))
                .isInstanceOf(OrderRepositoryException.class);

        Mockito.verify(productService).decreaseStock(productId, orderRequestDTO.quantity());
        Mockito.verify(productService).increaseStock(productId, orderRequestDTO.quantity()); // 보상 트랜잭션 검증
    }

    @ParameterizedTest
    @MethodSource("notPayableSource")
    void isPayable(OrderState ordersState, Integer pastMinutes, Boolean expectedPayable) {
        // given
        OrderEntity order = new OrderEntity(1L, 1L, 1, 1L, ordersState);
        ReflectionTestUtils.setField(order, "createdTime", LocalDateTime.now().minusMinutes(pastMinutes));

        // when
        Mockito.when(orderConfig.validTime()).thenReturn(5);
        Boolean result = orderService.isPayable(order);

        // then
        Assertions.assertThat(result).isEqualTo(expectedPayable);
    }

    private static Stream<Arguments> notPayableSource() {
        return Stream.of(
                Arguments.of(OrderState.CREATED, 4, true),
                Arguments.of(OrderState.ERROR, 4, false),
                Arguments.of(OrderState.CANCELED, 4, false),
                Arguments.of(OrderState.COMPLETED, 4, false),
                Arguments.of(OrderState.CREATED, 6, false)
        );
    }
}