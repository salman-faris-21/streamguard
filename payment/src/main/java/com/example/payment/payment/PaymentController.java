package com.example.payment.payment;


import com.example.payment.payment.dto.PaymentRequestDTO;
import com.example.payment.payment.dto.PaymentResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    public static final String USER_ID_HEADER = "X-User-Id";

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment(
            @Valid @RequestBody PaymentRequestDTO request,
            @RequestHeader("Idempotency-Key") @NotNull String idempotencyKey) {

        PaymentResponseDTO response = paymentService.createPayment(request, idempotencyKey);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDTO> getPayment(
            @PathVariable UUID paymentId,
            @RequestHeader(value = USER_ID_HEADER, required = false) String customerId) {

        return ResponseEntity.ok(paymentService.getPayment(paymentId, customerId));
    }
}
