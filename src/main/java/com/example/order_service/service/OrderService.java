package com.example.order_service.service;

import com.example.order_service.domain.Order;
import com.example.order_service.domain.OrderStatus;
import com.example.order_service.domain.Payment;
import com.example.order_service.domain.PaymentStatus;
import com.example.order_service.dto.CreateOrderRequest;
import com.example.order_service.dto.CreateOrderResponse;
import com.example.order_service.dto.OrderResponse;
import com.example.order_service.dto.PaymentCallbackRequest;
import com.example.order_service.exception.ResourceNotFoundException;
import com.example.order_service.messaging.PaymentMessagePublisher;
import com.example.order_service.messaging.PaymentStatusMessage;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.repository.PaymentRepository;
import java.util.UUID;
import org.springframework.amqp.AmqpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMessagePublisher paymentMessagePublisher;
    private final NotificationService notificationService;

    public OrderService(OrderRepository orderRepository,
                        PaymentRepository paymentRepository,
                        PaymentMessagePublisher paymentMessagePublisher,
                        NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.paymentMessagePublisher = paymentMessagePublisher;
        this.notificationService = notificationService;
    }

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order(request.getCustomerId(), request.getAmount());
        order.markPaymentPending();
        order = orderRepository.save(order);

        String paymentId = UUID.randomUUID().toString();
        Payment payment = new Payment(paymentId, order.getId(), order.getAmount());
        paymentRepository.save(payment);

        try {
            paymentMessagePublisher.publishPaymentRequest(payment);
        } catch (AmqpException ex) {
            log.error("Failed to publish payment request for order {}", order.getId(), ex);
            payment.markFailed();
            order.markFailed();
        }

        return new CreateOrderResponse(order.getId(), payment.getPaymentId(), order.getStatus(), order.getAmount());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order %s not found".formatted(orderId)));
        return mapToOrderResponse(order);
    }

    @Transactional
    public void handlePaymentCallback(PaymentCallbackRequest request) {
        processPaymentUpdate(request.getPaymentId(), request.getOrderId(), request.getAmount(), request.getStatus());
    }

    @Transactional
    public void handlePaymentStatusMessage(PaymentStatusMessage message) {
        processPaymentUpdate(message.paymentId(), message.orderId(), message.amount(), message.status());
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return new OrderResponse(order.getId(), order.getCustomerId(), order.getAmount(),
                order.getStatus(), order.getCreatedAt(), order.getUpdatedAt());
    }

    private void processPaymentUpdate(String paymentId, String orderId, java.math.BigDecimal amount, PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment %s not found".formatted(paymentId)));

        if (!payment.getOrderId().equals(orderId)) {
            throw new IllegalArgumentException("Payment does not belong to order");
        }

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order %s not found".formatted(payment.getOrderId())));

        if (payment.getAmount().compareTo(amount) != 0) {
            throw new IllegalArgumentException("Payment amount mismatch");
        }

        PaymentStatus currentStatus = payment.getStatus();

        if (status == currentStatus) {
            log.info("Idempotent update ignored for payment {} with status {}", payment.getPaymentId(), status);
            return;
        }

        if (currentStatus == PaymentStatus.SUCCESS) {
            log.warn("Ignoring conflicting update for already successful payment {} with status {}", payment.getPaymentId(), status);
            return;
        }

        switch (status) {
            case SUCCESS -> {
                payment.markSuccess();
                order.markPaid();
                notificationService.notifyPaymentSuccess(order);
            }
            case FAILED -> {
                payment.markFailed();
                order.markFailed();
            }
            default -> payment.markUnknown();
        }

        log.info("Payment {} transitioned from {} to {}", payment.getPaymentId(), currentStatus, status);
    }
}
