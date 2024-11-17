package com.project.yogerOrder.payment.service;

import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.entity.OrderState;
import com.project.yogerOrder.order.service.OrderService;
import com.project.yogerOrder.payment.dto.request.ConfirmPaymentRequestDTO;
import com.project.yogerOrder.payment.dto.request.VerifyPaymentRequestDTO;
import com.project.yogerOrder.payment.exception.InvalidPaymentRequestException;
import com.project.yogerOrder.payment.exception.PaymentAlreadyExistException;
import com.project.yogerOrder.payment.repository.PaymentRepository;
import com.project.yogerOrder.payment.util.pg.dto.request.PGRefundRequestDTO;
import com.project.yogerOrder.payment.util.pg.dto.resposne.PGPaymentInformResponseDTO;
import com.project.yogerOrder.payment.util.pg.enums.PGState;
import com.project.yogerOrder.payment.util.pg.service.PGClientService;
import com.project.yogerOrder.product.dto.response.ProductResponseDTO;
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
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentTransactionService paymentTransactionService;

    @Mock
    private PGClientService pgClientService;

    @Mock
    private OrderService orderService;

    @Mock
    private ProductService productService;


    String impUid = "test_imp";
    String merchantUid = "12345";
    Integer productPrice = 1000;
    Integer quantity = 3;
    Integer totalAmount = productPrice * quantity;
    Integer confirmedAmountPerQuantity = (int) (productPrice * 0.9);

    Long orderId = 1L;
    Long productId = 1L;
    Long userId = 1L;


    VerifyPaymentRequestDTO requestDTO = new VerifyPaymentRequestDTO(impUid, merchantUid);
    PGPaymentInformResponseDTO pgInform = new PGPaymentInformResponseDTO(impUid, merchantUid, totalAmount, PGState.PAID);
    ProductResponseDTO productResponseDTO = new ProductResponseDTO(productId, confirmedAmountPerQuantity, productPrice);
    OrderEntity orderEntity = new OrderEntity(orderId, productId, quantity, userId, OrderState.PENDING);


    @Test
    void verifySuccess() {
        // given
        given(paymentRepository.existsByPgPaymentId(any())).willReturn(false);
        given(pgClientService.getInformById(any())).willReturn(pgInform);
        given(orderService.findById(any())).willReturn(orderEntity);
        given(productService.findById(any())).willReturn(productResponseDTO);
        given(orderService.isPayable(any())).willReturn(true);
        willDoNothing().given(pgClientService).refund(any(PGRefundRequestDTO.class));
        willDoNothing().given(paymentTransactionService).confirmPaymentAndOrder(any(ConfirmPaymentRequestDTO.class));

        // when
        paymentService.verifyPayment(requestDTO);

        // then
        verify(pgClientService, times(0)).refund(any(PGRefundRequestDTO.class));
        verify(paymentTransactionService).confirmPaymentAndOrder(any(ConfirmPaymentRequestDTO.class));
    }

    @Test
    void verifyFailByPaymentExists() {
        // given
        given(paymentRepository.existsByPgPaymentId(requestDTO.impUid())).willReturn(true);

        // when, then
        assertThrows(PaymentAlreadyExistException.class, () -> paymentService.verifyPayment(requestDTO));
    }

    @ParameterizedTest
    @MethodSource("verifyFailByStateSource")
    void verifyFailByState(PGState pgState) {
        // given
        PGPaymentInformResponseDTO currentPGInform = new PGPaymentInformResponseDTO(impUid, merchantUid, productPrice, pgState);
        given(paymentRepository.existsByPgPaymentId(any())).willReturn(false);
        given(pgClientService.getInformById(any())).willReturn(currentPGInform);

        // when, then
        assertThrows(InvalidPaymentRequestException.class, () -> paymentService.verifyPayment(requestDTO));
    }

    private static Stream<Arguments> verifyFailByStateSource() {
        return Arrays.stream(PGState.values())
                .filter(state -> state != PGState.PAID)
                .map(Arguments::of);
    }


    @Test
    void verifyFailByAmount() {
        // given
        ArgumentCaptor<PGRefundRequestDTO> captor = ArgumentCaptor.forClass(PGRefundRequestDTO.class);

        given(paymentRepository.existsByPgPaymentId(any())).willReturn(false);
        given(pgClientService.getInformById(any())).willReturn(pgInform);
        given(orderService.findById(any())).willReturn(orderEntity);
        given(productService.findById(any())).willReturn(productResponseDTO);
        lenient().when(orderService.isPayable(any())).thenReturn(true);
        willDoNothing().given(pgClientService).refund(any(PGRefundRequestDTO.class));

        // when, then
        assertThrows(InvalidPaymentRequestException.class, () -> paymentService.verifyPayment(requestDTO));
        verify(pgClientService).refund(captor.capture());
        Assertions.assertThat(captor.getValue().paymentId()).isEqualTo(pgInform.pgPaymentId());
        Assertions.assertThat(captor.getValue().checksum()).isEqualTo(pgInform.amount());
        Assertions.assertThat(captor.getValue().refundAmount()).isEqualTo(pgInform.amount());
        verify(paymentTransactionService, times(0)).confirmPaymentAndOrder(any(ConfirmPaymentRequestDTO.class));
    }
}