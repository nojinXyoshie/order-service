package com.example.order_service.controller;

import com.example.order_service.dto.PaymentCallbackRequest;
import com.example.order_service.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final OrderService orderService;

    public PaymentController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/callback")
    public ResponseEntity<Void> handleCallback(@RequestBody @Valid PaymentCallbackRequest request) {
        log.info("Received payment callback for payment {} with status {}", request.getPaymentId(), request.getStatus());
        orderService.handlePaymentCallback(request);
        return ResponseEntity.ok().build();
    }
}
