package com.example.payment.payment.event;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class PaymentEventProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public PaymentEventProducer(
            KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, PaymentEvent>>
    publishPaymentEvent(PaymentEvent event) {

        return kafkaTemplate.send(
                "payment-events",
                event.getOrderId().toString(),
                event
        );
    }
}