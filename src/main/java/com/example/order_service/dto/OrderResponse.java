package com.example.order_service.dto;

import com.example.order_service.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class OrderResponse {

    private final String orderId;
    private final String customerId;
    private final BigDecimal amount;
    private final OrderStatus status;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public OrderResponse(String orderId, String customerId, BigDecimal amount,
                         OrderStatus status, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
