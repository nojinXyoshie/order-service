package com.example.order_service.messaging;

import com.example.order_service.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentStatusListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentStatusListener.class);

    private final OrderService orderService;

    public PaymentStatusListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = "${messaging.payment.status.queue}")
    public void handlePaymentStatus(PaymentStatusMessage message) {
        log.info("Received payment status message for payment {} with status {}", message.paymentId(), message.status());
        orderService.handlePaymentStatusMessage(message);
    }
}
