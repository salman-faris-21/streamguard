package com.example.payment.payment;


import com.example.payment.payment.dto.PaymentRequestDTO;
import com.example.payment.payment.dto.PaymentResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment(
            @Valid @RequestBody PaymentRequestDTO request,
            @RequestHeader("Idempotency-Key") @NotNull String idempotencyKey) {

        PaymentResponseDTO response = paymentService.createPayment(request,idempotencyKey);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}