package com.project.yogerOrder.payment.controller;

import com.project.yogerOrder.payment.dto.request.PartialRefundRequestDTOs;
import com.project.yogerOrder.payment.dto.request.PortOnePaymentWebhookRequestDTO;
import com.project.yogerOrder.payment.dto.request.VerifyPaymentRequestDTO;
import com.project.yogerOrder.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/verify")
    public ResponseEntity<Void> verifyPaymentWebhook(@RequestBody @Valid PortOnePaymentWebhookRequestDTO portOnePaymentWebhookRequestDTO) {
        paymentService.verifyPayment(VerifyPaymentRequestDTO.from(portOnePaymentWebhookRequestDTO));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/products/expire")
    public ResponseEntity<Void> productExpired(@RequestBody @Valid PartialRefundRequestDTOs partialRefundRequestDTOs) {
        paymentService.productsExpiration(partialRefundRequestDTOs);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
