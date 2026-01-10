package com.example.order_service.dto;

import com.example.order_service.domain.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PaymentCallbackRequest {

    @NotBlank
    private String paymentId;

    @NotBlank
    private String orderId;

    @NotNull
    private PaymentStatus status;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal amount;

    private String signature;

    public PaymentCallbackRequest() {
    }

    public PaymentCallbackRequest(String paymentId, String orderId, PaymentStatus status, BigDecimal amount) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.status = status;
        this.amount = amount;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
