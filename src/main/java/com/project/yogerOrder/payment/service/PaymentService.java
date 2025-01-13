package com.project.yogerOrder.payment.service;

import com.project.yogerOrder.order.entity.OrderEntity;
import com.project.yogerOrder.order.service.OrderService;
import com.project.yogerOrder.payment.dto.request.ConfirmPaymentRequestDTO;
import com.project.yogerOrder.payment.dto.request.PartialRefundRequestDTO;
import com.project.yogerOrder.payment.dto.request.PartialRefundRequestDTOs;
import com.project.yogerOrder.payment.dto.request.VerifyPaymentRequestDTO;
import com.project.yogerOrder.payment.dto.response.PaymentOrderDTO;
import com.project.yogerOrder.payment.entity.PaymentEntity;
import com.project.yogerOrder.payment.event.PaymentEventProducer;
import com.project.yogerOrder.payment.repository.PaymentRepository;
import com.project.yogerOrder.payment.util.pg.dto.request.PGRefundRequestDTO;
import com.project.yogerOrder.payment.util.pg.dto.resposne.PGPaymentInformResponseDTO;
import com.project.yogerOrder.payment.util.pg.service.PGClientService;
import com.project.yogerOrder.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final PaymentTransactionService paymentTransactionService;

    private final PGClientService pgClientService;

    private final OrderService orderService;

    private final ProductService productService;

    private final PaymentEventProducer paymentEventProducer;


    // 웹훅인 줄 알았는데 외부 요청이었으면 -> 상관 없게 로직 작성
    // 신뢰 정보(= 사용자 조작 불가, 검증 필요): 결제 id(존재 검증), 결제 금액(원래 값과 비교), 결제 상태(paid 상태인지 검사)
    // 비신뢰 정보(= 사용자 조작 가능, 검증 필요): 주문 id(다른 주문 결제 검증 필요 X)
    public void verifyPayment(VerifyPaymentRequestDTO verifyPaymentRequestDTO) {
        // 결제 id 존재 검증
        if (paymentRepository.existsByPgPaymentId(verifyPaymentRequestDTO.impUid())) { // 내부
            log.debug("Verifying payment {} is ignored because already exists", verifyPaymentRequestDTO.impUid());
            return;
        }

        PGPaymentInformResponseDTO pgInform = pgClientService.getInformById(verifyPaymentRequestDTO.impUid()); // 외부
        OrderEntity orderEntity = orderService.findById(Long.valueOf(pgInform.orderId())); // 내부
        if (!pgInform.isPaid()) { // 결제된 상태가 아니면 환불 X
            log.error("PG payment {} is not paid state", pgInform.pgPaymentId());
            PaymentEntity errorPayment = PaymentEntity.createCanceledPayment(
                    pgInform.pgPaymentId(),
                    Long.valueOf(pgInform.orderId()),
                    pgInform.amount(),
                    orderEntity.getBuyerId()
            );
            paymentRepository.save(errorPayment);

            paymentEventProducer.sendEventByState(errorPayment);

            return;
        }

        // 결제 검증 = 금액, 상태
        if (!orderService.isPayable(orderEntity)) { // 내부
            log.debug("payment {} is not payable", pgInform.pgPaymentId());
            PaymentEntity canceledPayment = PaymentEntity.createCanceledPayment(
                    pgInform.pgPaymentId(),
                    Long.valueOf(pgInform.orderId()),
                    pgInform.amount(),
                    orderEntity.getBuyerId()
            );
            paymentRepository.save(canceledPayment);

            pgClientService.refund(new PGRefundRequestDTO(pgInform.pgPaymentId(), pgInform.amount())); // 외부

            paymentEventProducer.sendEventByState(canceledPayment);

            return;
        }

        Integer originalMaxPrice = productService.findById(orderEntity.getProductId()).originalMaxPrice();
        if (pgInform.amount() != (originalMaxPrice * orderEntity.getQuantity())) { // 내부
            log.error("PG payment {} is invalid", pgInform.pgPaymentId());
            PaymentEntity errorPayment = PaymentEntity.createErrorPayment(
                    pgInform.pgPaymentId(),
                    Long.valueOf(pgInform.orderId()),
                    pgInform.amount(),
                    orderEntity.getBuyerId()
            );
            paymentRepository.save(errorPayment);

            pgClientService.refund(new PGRefundRequestDTO(pgInform.pgPaymentId(), pgInform.amount())); // 외부

            paymentEventProducer.sendEventByState(errorPayment);

            return;
        }

        paymentTransactionService.confirmPayment(new ConfirmPaymentRequestDTO( // 내부
                pgInform.pgPaymentId(),
                Long.valueOf(pgInform.orderId()),
                orderEntity.getBuyerId(),
                pgInform.amount()
        ));
    }

    @Transactional
    public void orderCanceled(Long orderId) {
        paymentRepository.findByOrderId(orderId).ifPresent(paymentEntity -> {
            Boolean isUpdated = paymentEntity.updateToCanceledState();
            if (!isUpdated) {
                log.debug("payment {} is already canceled", paymentEntity.getId());
                return;
            }
            paymentRepository.save(paymentEntity);

            pgClientService.refund(new PGRefundRequestDTO(paymentEntity.getPgPaymentId(), paymentEntity.getAmount()));

            paymentEventProducer.sendEventByState(paymentEntity);
        });
    }

    public void productsExpiration(PartialRefundRequestDTOs partialRefundRequestDTOs) {
        partialRefundRequestDTOs.partialRefundRequestDTOs().forEach(this::expirationRefundPerProduct);
    }

    private void expirationRefundPerProduct(PartialRefundRequestDTO partialRefundRequestDTO) {
        Long productId = partialRefundRequestDTO.productId();
        for (PaymentOrderDTO paymentOrderDTO : paymentRepository.findAllPaymentAndOrderByProductId(productId)) {
            PaymentEntity payment = paymentOrderDTO.payment();
            OrderEntity order = paymentOrderDTO.order();
            int refundAmountPerQuantity = partialRefundRequestDTO.originalMaxPrice() - partialRefundRequestDTO.confirmedPrice();
            int refundAmount = refundAmountPerQuantity * order.getQuantity();
            int checksum = payment.getAmount();

            if (!payment.isPayCompletable()) {
                log.error("payment {} is in invalid state", payment.getId());
                updateByError(payment);
                continue;
            }
            if (!Objects.equals(payment.getAmount(), partialRefundRequestDTO.originalMaxPrice() * order.getQuantity())
                    || !payment.isPartialRefundable(refundAmount)) {
                log.error("Failed to partial refund payment {} from PG", payment.getId());
                updateByError(payment);
                continue;
            }

            // 외부 API 장애에 따라 db state가 영향받지 않도록, 외부 API 호출이 완료되면 entity를 update하도록 작성
            pgClientService.refund(new PGRefundRequestDTO(payment.getPgPaymentId(), checksum, refundAmount));
            paymentTransactionService.refund(payment, refundAmount);
        }
    }

    private void updateByError(PaymentEntity payment) {
        Boolean isUpdated = payment.updateToErrorState();
        if (!isUpdated) {
            log.debug("payment {} is already errored", payment.getId());
            return;
        }

        paymentRepository.save(payment);

        paymentEventProducer.sendEventByState(payment);
    }
}
