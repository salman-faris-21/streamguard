package com.example.payment.payment.impl;

import com.example.payment.payment.Payment;
import com.example.payment.payment.PaymentRepository;
import com.example.payment.payment.PaymentService;
import com.example.payment.payment.dto.PaymentRequestDTO;
import com.example.payment.payment.dto.PaymentResponseDTO;
import com.example.payment.payment.enums.PaymentStatus;
import com.example.payment.payment.event.OutboxEvent;
import com.example.payment.payment.event.OutboxEventRepository;
import com.example.payment.payment.event.PaymentEvent;
import com.example.payment.payment.mapper.PaymentMapper;
import com.example.payment.payment.orderEntity.OrderResponseDTO;
import com.example.payment.payment.orderEntity.OrderStatus;
import com.example.payment.payment.client.OrderClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Order;

import org.json.JSONObject;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final RazorpayClient razorpayClient;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            OrderClient orderClient,
            RazorpayClient razorpayClient,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {

        this.paymentRepository = paymentRepository;
        this.orderClient = orderClient;
        this.razorpayClient = razorpayClient;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }


    // =========================================================
    // CREATE PAYMENT
    // =========================================================

    @Override
    public PaymentResponseDTO createPayment(
            PaymentRequestDTO request,
            String idempotencyKey) {

        // 1. Retrieve order details through Order Service
        OrderResponseDTO order =
                orderClient.getOrderById(request.getOrderId());


        // 2. Check idempotency
        Optional<Payment> existingPayment =
                paymentRepository.findByIdempotencyKey(idempotencyKey);

        if (existingPayment.isPresent()) {

            Payment existing = existingPayment.get();

            boolean sameRequest =
                    existing.getOrderId().equals(order.getId()) &&
                            existing.getAmount().compareTo(order.getTotalAmount()) == 0 &&
                            existing.getCurrency().equalsIgnoreCase(order.getCurrency());

            if (!sameRequest) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Idempotency key already used with a different payment request"
                );
            }

            return PaymentMapper.toResponseDTO(existing);
        }


        // 3. Validate order status
        if (order.getStatus() != OrderStatus.PENDING) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment can only be initiated for PENDING orders"
            );
        }


        // 4. Create internal payment
        Payment payment = new Payment();

        payment.setOrderId(order.getId());
        payment.setAmount(order.getTotalAmount());
        payment.setCurrency(order.getCurrency());
        payment.setIdempotencyKey(idempotencyKey);
        payment.setStatus(PaymentStatus.PENDING);


        // 5. Save internal payment
        Payment savedPayment =
                paymentRepository.saveAndFlush(payment);


        try {

            // 6. Convert INR amount into paise
            int amountInPaise = savedPayment.getAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.UNNECESSARY)
                    .intValueExact();


            // 7. Prepare Razorpay order request
            JSONObject razorpayRequest = new JSONObject();

            razorpayRequest.put("amount", amountInPaise);
            razorpayRequest.put("currency", savedPayment.getCurrency());

            razorpayRequest.put(
                    "receipt",
                    savedPayment.getId().toString()
            );


            // 8. Create Razorpay order
            Order razorpayOrder =
                    razorpayClient.orders.create(razorpayRequest);


            // 9. Store Razorpay Order ID
            savedPayment.setRazorpayOrderId(
                    razorpayOrder.get("id")
            );

            paymentRepository.save(savedPayment);


            // 10. Return payment response
            return PaymentMapper.toResponseDTO(savedPayment);

        } catch (RazorpayException e) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to create Razorpay order",
                    e
            );

        } catch (ArithmeticException e) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid payment amount",
                    e
            );
        }
    }


    // =========================================================
    // PROCESS RAZORPAY WEBHOOK
    // =========================================================

    @Override
    public void processWebhook(
            String event,
            String razorpayPaymentId,
            String razorpayOrderId,
            String paymentStatus,
            long amount,
            String currency) {


        // 1. Find internal payment using Razorpay Order ID
        Payment payment = paymentRepository
                .findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Payment not found"
                        )
                );


        // 2. Convert Razorpay amount from paise to rupees
        BigDecimal razorpayAmount =
                BigDecimal.valueOf(amount, 2);


        // 3. Validate amount
        if (payment.getAmount().compareTo(razorpayAmount) != 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment amount mismatch"
            );
        }


        // 4. Validate currency
        if (!payment.getCurrency().equalsIgnoreCase(currency)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment currency mismatch"
            );
        }


        // 5. Determine payment outcome
        PaymentStatus newStatus;

        if ("payment.captured".equals(event)
                && "captured".equals(paymentStatus)) {

            newStatus = PaymentStatus.SUCCESS;

        } else if ("payment.failed".equals(event)
                && "failed".equals(paymentStatus)) {

            newStatus = PaymentStatus.FAILED;

        } else {

            // Ignore webhook events that we don't handle
            return;
        }


        // 6. Prevent duplicate webhook processing
        if (payment.getStatus() == newStatus) {
            return;
        }


        // 7. Update internal Payment
        payment.setStatus(newStatus);

        paymentRepository.save(payment);


        // 8. Create PaymentEvent
        PaymentEvent paymentEvent = new PaymentEvent();

        paymentEvent.setPaymentId(payment.getId());
        paymentEvent.setOrderId(payment.getOrderId());
        paymentEvent.setAmount(payment.getAmount());
        paymentEvent.setCurrency(payment.getCurrency());
        paymentEvent.setStatus(String.valueOf(newStatus));


        // 9. Convert PaymentEvent into JSON
        String payload;

        try {

            payload = objectMapper.writeValueAsString(paymentEvent);

        } catch (JsonProcessingException e) {

            throw new RuntimeException(
                    "Failed to serialize PaymentEvent",
                    e
            );
        }


        // 10. Create OutboxEvent
        OutboxEvent outboxEvent = new OutboxEvent();

        outboxEvent.setAggregateId(
                payment.getId()
        );

        outboxEvent.setEventType(
                "PAYMENT_" + newStatus
        );

        outboxEvent.setPayload(payload);

        outboxEvent.setStatus("PENDING");

        outboxEvent.setCreatedAt(
                LocalDateTime.now()
        );


        // 11. Save Outbox Event
        outboxEventRepository.save(outboxEvent);
    }
}