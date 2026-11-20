package com.example.payment.payment;


import com.example.payment.payment.dto.PaymentRequestDTO;
import com.example.payment.payment.dto.PaymentResponseDTO;

public interface PaymentService {
    PaymentResponseDTO createPayment(PaymentRequestDTO request, String idempotencyKey);

    void processWebhook(
            String event,
            String razorpayPaymentId,
            String razorpayOrderId,
            String paymentStatus,
            long amount,
            String currency
    );

    // Create payment
    // Get payment by ID
}