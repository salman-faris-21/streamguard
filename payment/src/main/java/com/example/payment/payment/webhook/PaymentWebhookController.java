package com.example.payment.payment.webhook;

import com.example.payment.payment.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@RestController
@RequestMapping("/api/webhooks")
public class PaymentWebhookController {

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;

    public PaymentWebhookController(
            ObjectMapper objectMapper,
            PaymentService paymentService) {

        this.objectMapper = objectMapper;
        this.paymentService = paymentService;
    }

    @PostMapping("/razorpay")
    public ResponseEntity<String> handleRazorpayWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        // 1. Verify Razorpay webhook signature
        boolean isValid = verifySignature(payload, signature);

        if (!isValid) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid webhook signature");
        }

        // 2. Parse webhook payload
        try {
            JsonNode root = objectMapper.readTree(payload);

            String event = root.path("event").asText();

            JsonNode paymentEntity = root
                    .path("payload")
                    .path("payment")
                    .path("entity");

            String razorpayPaymentId =
                    paymentEntity.path("id").asText();

            String razorpayOrderId =
                    paymentEntity.path("order_id").asText();

            String paymentStatus =
                    paymentEntity.path("status").asText();

            long amount =
                    paymentEntity.path("amount").asLong();

            String currency =
                    paymentEntity.path("currency").asText();

            // 3. Delegate payment processing to Service Layer
            paymentService.processWebhook(
                    event,
                    razorpayPaymentId,
                    razorpayOrderId,
                    paymentStatus,
                    amount,
                    currency
            );

            // 4. Return success if processing completes
            return ResponseEntity.ok("Webhook processed successfully");

        } catch (JsonProcessingException exception) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid webhook payload");
        }
    }

    private boolean verifySignature(
            String payload,
            String signature) {

        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey = new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );

            mac.init(secretKey);

            byte[] expectedSignature = mac.doFinal(
                    payload.getBytes(StandardCharsets.UTF_8)
            );

            byte[] receivedSignature =
                    HexFormat.of().parseHex(signature);

            return MessageDigest.isEqual(
                    expectedSignature,
                    receivedSignature
            );

        } catch (Exception exception) {
            return false;
        }
    }
}