package com.project.yogerOrder.payment.service;

import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.service.OrderService;
import com.project.yogerOrder.payment.dto.request.ConfirmPaymentRequestDTO;
import com.project.yogerOrder.payment.dto.request.PartialRefundRequestDTO;
import com.project.yogerOrder.payment.dto.request.PartialRefundRequestDTOs;
import com.project.yogerOrder.payment.dto.request.VerifyPaymentRequestDTO;
import com.project.yogerOrder.payment.dto.response.PaymentOrderDTO;
import com.project.yogerOrder.payment.entity.PaymentEntity;
import com.project.yogerOrder.payment.repository.PaymentRepository;
import com.project.yogerOrder.payment.util.pg.dto.request.PGRefundRequestDTO;
import com.project.yogerOrder.payment.util.pg.dto.resposne.PGPaymentInformResponseDTO;
import com.project.yogerOrder.payment.util.pg.enums.PGState;
import com.project.yogerOrder.payment.util.pg.service.PGClientService;
import com.project.yogerOrder.product.dto.response.PriceByQuantity;
import com.project.yogerOrder.product.dto.response.ProductResponseDTO;
import com.project.yogerOrder.product.service.ProductService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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

    private TestSource source1;
    private TestSource source2;
    private TestSource source3;

    private static class TestSource {
        String impUid;
        String merchantUid;
        Integer productPrice;
        Integer quantity;
        Integer totalAmount;
        Integer confirmedAmountPerQuantity;
        Integer refundAmountPerQuantity;
        Integer refundAmount;

        Long orderId;
        Long productId;
        Long userId;

        VerifyPaymentRequestDTO requestDTO;
        PGPaymentInformResponseDTO pgInform;
        ProductResponseDTO productResponseDTO;
        PaymentEntity paymentEntity;
        OrderEntity orderEntity;
        PaymentOrderDTO paymentOrderDTO;

        public TestSource(String impUid, String merchantUid, Integer productPrice, Integer quantity, Long productId, Long userId) {
            this.impUid = impUid;
            this.merchantUid = merchantUid;
            this.productPrice = productPrice;
            this.quantity = quantity;
            this.orderId = Long.valueOf(merchantUid);
            this.productId = productId;
            this.userId = userId;

            this.totalAmount = productPrice * quantity;
            this.confirmedAmountPerQuantity = (int) (productPrice * 0.9);
            this.refundAmountPerQuantity = productPrice - confirmedAmountPerQuantity;
            this.refundAmount = refundAmountPerQuantity * quantity;


            this.requestDTO = new VerifyPaymentRequestDTO(impUid, merchantUid);
            this.pgInform = new PGPaymentInformResponseDTO(impUid, merchantUid, totalAmount, PGState.PAID);
            PriceByQuantity priceByQuantity1 = new PriceByQuantity(3, productPrice);
            PriceByQuantity priceByQuantity2 = new PriceByQuantity(10, (int) (productPrice * 0.9));
            this.productResponseDTO = new ProductResponseDTO(productId, List.of(priceByQuantity1, priceByQuantity2));
            this.paymentEntity = PaymentEntity.createTempPaidPayment(impUid, orderId, totalAmount, userId);
            this.orderEntity = OrderEntity.createPendingOrder(productId, quantity, userId);
            ReflectionTestUtils.setField(orderEntity, "id", orderId);
            this.paymentOrderDTO = new PaymentOrderDTO(paymentEntity, orderEntity);
        }
    }


    @BeforeEach
    void beforeEach() {
        this.source1 = new TestSource("impUid1", "123456", 10000, 2, 11L, 111L);
        this.source2 = new TestSource("impUid2", "654321", 3000, 5, 22L, 222L);
        this.source3 = new TestSource("impUid3", "321456", 3000, 5, 22L, 222L);
    }


    @Test
    void verifySuccess() {
        // given
        ArgumentCaptor<ConfirmPaymentRequestDTO> captor = ArgumentCaptor.forClass(ConfirmPaymentRequestDTO.class);

        given(paymentRepository.existsByPgPaymentId(any())).willReturn(false);
        given(pgClientService.getInformById(any())).willReturn(source1.pgInform);
        given(orderService.findById(any())).willReturn(source1.orderEntity);
        given(productService.findById(any())).willReturn(source1.productResponseDTO);
        given(orderService.isPayable(any())).willReturn(true);

        // when
        paymentService.verifyPayment(source1.requestDTO);

        // then
        verify(pgClientService, times(0)).refund(any(PGRefundRequestDTO.class));
        verify(paymentTransactionService).confirmPaymentAndOrder(captor.capture());

        ConfirmPaymentRequestDTO confirmPaymentRequestDTO = captor.getValue();
        assertEquals(confirmPaymentRequestDTO.pgPaymentId(), source1.pgInform.pgPaymentId());
        assertEquals(confirmPaymentRequestDTO.orderId(), source1.orderId);
        assertEquals(confirmPaymentRequestDTO.buyerId(), source1.userId);
        assertEquals(confirmPaymentRequestDTO.amount(), source1.totalAmount);
    }

    @Test
    void verifyFailByPaymentExists() {
        // given
        given(paymentRepository.existsByPgPaymentId(source1.impUid)).willReturn(true);

        // when
        paymentService.verifyPayment(source1.requestDTO);

        // then
        verify(pgClientService, times(0)).getInformById(any());
        verify(orderService, times(0)).findById(any());
        verify(productService, times(0)).findById(any());
        verify(pgClientService, times(0)).refund(any(PGRefundRequestDTO.class));
        verify(paymentTransactionService, times(0)).confirmPaymentAndOrder(any(ConfirmPaymentRequestDTO.class));
    }

    @ParameterizedTest
    @MethodSource("verifyFailByStateSource")
    void verifyFailByState(PGState pgState) {
        // given
        PGPaymentInformResponseDTO currentPGInform = new PGPaymentInformResponseDTO(source1.impUid, source1.merchantUid, source1.productPrice, pgState);
        given(paymentRepository.existsByPgPaymentId(any())).willReturn(false);
        given(pgClientService.getInformById(any())).willReturn(currentPGInform);

        // when
        paymentService.verifyPayment(source1.requestDTO);

        // then
        verify(orderService, times(0)).findById(any());
        verify(productService, times(0)).findById(any());
        verify(pgClientService, times(0)).refund(any(PGRefundRequestDTO.class));
        verify(paymentTransactionService, times(0)).confirmPaymentAndOrder(any(ConfirmPaymentRequestDTO.class));
    }

    private static Stream<Arguments> verifyFailByStateSource() {
        return Arrays.stream(PGState.values())
                .filter(state -> !state.isPaid())
                .map(Arguments::of);
    }


    @Test
    void verifyFailByAmount() {
        // given
        ArgumentCaptor<PGRefundRequestDTO> captor = ArgumentCaptor.forClass(PGRefundRequestDTO.class);

        PGPaymentInformResponseDTO invalidPGInform = new PGPaymentInformResponseDTO(
                source1.impUid, source1.merchantUid, (int) (source1.pgInform.amount() * 0.1), PGState.PAID
        );

        given(paymentRepository.existsByPgPaymentId(any())).willReturn(false);
        given(pgClientService.getInformById(any())).willReturn(invalidPGInform);
        given(orderService.findById(any())).willReturn(source1.orderEntity);
        given(productService.findById(any())).willReturn(source1.productResponseDTO);
        lenient().when(orderService.isPayable(source1.orderEntity)).thenReturn(true);

        // when
        paymentService.verifyPayment(source1.requestDTO);

        // when, then
        verify(pgClientService, times(1)).refund(captor.capture());
        Assertions.assertThat(captor.getValue().paymentId()).isEqualTo(source1.pgInform.pgPaymentId());
        Assertions.assertThat(captor.getValue().checksum()).isEqualTo(invalidPGInform.amount());
        Assertions.assertThat(captor.getValue().refundAmount()).isEqualTo(invalidPGInform.amount());
        verify(paymentTransactionService, times(0)).confirmPaymentAndOrder(any(ConfirmPaymentRequestDTO.class));
    }

    @Test
    void productsExpirationSuccess() {
        // given
        ArgumentCaptor<PGRefundRequestDTO> captor = ArgumentCaptor.forClass(PGRefundRequestDTO.class);

        // 첫 번 째 상품
        given(paymentRepository.findAllPaymentAndOrderByProductId(source1.productId)).willReturn(List.of(source1.paymentOrderDTO));
        PartialRefundRequestDTO partialRefundRequestDTO1 = new PartialRefundRequestDTO(source1.productId, source1.productPrice, source1.confirmedAmountPerQuantity);

        // 두 번 째 상품
        given(paymentRepository.findAllPaymentAndOrderByProductId(source2.productId)).willReturn(List.of(source2.paymentOrderDTO, source3.paymentOrderDTO));
        PartialRefundRequestDTO partialRefundRequestDTO2 = new PartialRefundRequestDTO(source2.productId, source2.productPrice, source2.confirmedAmountPerQuantity);

        // 첫 번 째 + 두 번 째 상품 종합
        PartialRefundRequestDTOs partialRefundRequestDTOs = new PartialRefundRequestDTOs(List.of(partialRefundRequestDTO1, partialRefundRequestDTO2));


        // when
        paymentService.productsExpiration(partialRefundRequestDTOs);

        // then
        verify(pgClientService, times(3)).refund(captor.capture());
        List<PGRefundRequestDTO> captured = captor.getAllValues();

        assertTrue(captured.contains(new PGRefundRequestDTO(source1.impUid, source1.totalAmount, source1.refundAmount)));
        assertTrue(captured.contains(new PGRefundRequestDTO(source2.impUid, source2.totalAmount, source2.refundAmount)));
        assertTrue(captured.contains(new PGRefundRequestDTO(source2.impUid, source3.totalAmount, source3.refundAmount)));

        ArgumentCaptor<PaymentEntity> paymentCaptor = ArgumentCaptor.forClass(PaymentEntity.class);
        ArgumentCaptor<Integer> integerCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(paymentTransactionService, times(3)).refund(paymentCaptor.capture(), integerCaptor.capture());

        List<PaymentEntity> capturedPayments = paymentCaptor.getAllValues();
        assertTrue(capturedPayments.contains(source1.paymentEntity));
        assertTrue(capturedPayments.contains(source2.paymentEntity));
        assertTrue(capturedPayments.contains(source3.paymentEntity));

        List<Integer> capturedAmounts = integerCaptor.getAllValues();
        assertTrue(capturedAmounts.contains(source1.refundAmount));
        assertTrue(capturedAmounts.contains(source2.refundAmount));
        assertTrue(capturedAmounts.contains(source3.refundAmount));
    }
}