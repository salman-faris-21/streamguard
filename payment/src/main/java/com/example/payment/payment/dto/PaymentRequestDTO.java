package com.example.payment.payment.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class PaymentRequestDTO {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }
}