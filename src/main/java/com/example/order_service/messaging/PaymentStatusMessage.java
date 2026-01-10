package com.example.order_service.messaging;

import com.example.order_service.domain.PaymentStatus;
import java.io.Serializable;
import java.math.BigDecimal;

public record PaymentStatusMessage(String paymentId,
                                   String orderId,
                                   BigDecimal amount,
                                   PaymentStatus status) implements Serializable {
}
