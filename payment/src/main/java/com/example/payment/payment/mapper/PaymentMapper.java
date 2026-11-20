
package com.example.payment.payment.mapper;

import com.example.payment.payment.Payment;
import com.example.payment.payment.dto.PaymentResponseDTO;

public class PaymentMapper {

    public static PaymentResponseDTO toResponseDTO(Payment payment) {

        PaymentResponseDTO response = new PaymentResponseDTO();

        response.setPaymentId(payment.getId());
        response.setOrderId(payment.getOrderId());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setStatus(payment.getStatus().name());

        // Razorpay integration
        response.setRazorpayOrderId(payment.getRazorpayOrderId());

        return response;
    }
}
