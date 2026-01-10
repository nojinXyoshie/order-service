package com.example.order_service.messaging;

import com.example.order_service.domain.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentMessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentMessagePublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String paymentRequestQueue;

    public PaymentMessagePublisher(RabbitTemplate rabbitTemplate,
                                   @Value("${messaging.payment.request.queue}") String paymentRequestQueue) {
        this.rabbitTemplate = rabbitTemplate;
        this.paymentRequestQueue = paymentRequestQueue;
    }

    public void publishPaymentRequest(Payment payment) {
        PaymentRequestMessage message = new PaymentRequestMessage(payment.getPaymentId(), payment.getOrderId(), payment.getAmount());
        rabbitTemplate.convertAndSend(paymentRequestQueue, message);
        log.info("Published payment request for order {} payment {}", payment.getOrderId(), payment.getPaymentId());
    }

    public record PaymentRequestMessage(String paymentId, String orderId, java.math.BigDecimal amount) {
    }
}
