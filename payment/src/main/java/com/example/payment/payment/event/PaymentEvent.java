package com.example.payment.payment.event;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentEvent {

    private UUID paymentId;
    private UUID orderId;
    private BigDecimal amount;
    private String currency;
    private String status;

    public PaymentEvent() {
    }

    public PaymentEvent(
            UUID paymentId,
            UUID orderId,
            BigDecimal amount,
            String currency,
            String status
    ) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }

    // Getters and setters
    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}