package com.example.payment.payment;


import com.example.payment.payment.dto.PaymentRequestDTO;
import com.example.payment.payment.dto.PaymentResponseDTO;

import java.util.UUID;

public interface PaymentService {
    PaymentResponseDTO createPayment(PaymentRequestDTO request, String idempotencyKey);

    PaymentResponseDTO getPayment(UUID paymentId, String customerId);

    void processWebhook(
            String event,
            String razorpayPaymentId,
            String razorpayOrderId,
            String paymentStatus,
            long amount,
            String currency
    );
}
