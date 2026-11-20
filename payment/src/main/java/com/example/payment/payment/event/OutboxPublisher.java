package com.example.payment.payment.event;



import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final PaymentEventProducer paymentEventProducer;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            PaymentEventProducer paymentEventProducer,
            ObjectMapper objectMapper) {

        this.outboxEventRepository = outboxEventRepository;
        this.paymentEventProducer = paymentEventProducer;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {

        List<OutboxEvent> pendingEvents =
                outboxEventRepository.findByStatus("PENDING");

        for (OutboxEvent outboxEvent : pendingEvents) {

            try {
                // 1. Convert JSON payload into PaymentEvent
                PaymentEvent event = objectMapper.readValue(
                        outboxEvent.getPayload(),
                        PaymentEvent.class
                );

                // 2. Publish event and wait for Kafka acknowledgement
                paymentEventProducer
                        .publishPaymentEvent(event)
                        .get(10, TimeUnit.SECONDS);

                // 3. Mark event as published
                outboxEvent.setStatus("PUBLISHED");
                outboxEvent.setPublishedAt(
                        java.time.LocalDateTime.now()
                );

                outboxEventRepository.save(outboxEvent);

                System.out.println(
                        "Outbox event published: " + outboxEvent.getId()
                );

            } catch (Exception exception) {

                System.err.println(
                        "Failed to publish outbox event: "
                                + outboxEvent.getId()
                );

                exception.printStackTrace();
            }
        }
    }
}