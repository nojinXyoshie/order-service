package com.example.order_service.service;

import com.example.order_service.domain.Payment;
import com.example.order_service.exception.PaymentGatewayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class PaymentGatewayClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentGatewayClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PaymentGatewayClient(RestTemplate restTemplate,
                                @Value("${payment.gateway.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Retryable(value = RestClientException.class,
               maxAttempts = 3,
               backoff = @Backoff(delay = 500, multiplier = 2.0))
    public void initiateCharge(Payment payment) {
        try {
            var request = new PaymentInitiationRequest(payment.getPaymentId(),
                    payment.getOrderId(), payment.getAmount());
            restTemplate.postForEntity(baseUrl + "/payments", request, Void.class);
            log.info("Payment initiation sent for order {}", payment.getOrderId());
        } catch (RestClientException ex) {
            log.error("Error calling payment gateway for order {}", payment.getOrderId(), ex);
            throw new PaymentGatewayException("Failed to initiate payment due to gateway error", ex);
        }
    }

    @Recover
    public void recover(RestClientException ex, Payment payment) {
        log.error("Payment gateway permanently failed after retries for order {}", payment.getOrderId(), ex);
        throw new PaymentGatewayException("Payment gateway unreachable after retries", ex);
    }

    private record PaymentInitiationRequest(String paymentId, String orderId, java.math.BigDecimal amount) {
    }
}
