package com.example.order_service.dto;

import com.example.order_service.domain.OrderStatus;
import java.math.BigDecimal;

public class CreateOrderResponse {

    private final String orderId;
    private final String paymentId;
    private final OrderStatus status;
    private final BigDecimal amount;

    public CreateOrderResponse(String orderId, String paymentId, OrderStatus status, BigDecimal amount) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.status = status;
        this.amount = amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
