package com.example.payment.payment.event;



import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final PaymentEventProducer paymentEventProducer;
    private final ObjectMapper objectMapper;
    private final int maxRetries;
    private final long initialBackoffMs;
    private final long maxBackoffMs;

    public OutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            PaymentEventProducer paymentEventProducer,
            ObjectMapper objectMapper,
            @Value("${streamguard.outbox.max-retries:8}") int maxRetries,
            @Value("${streamguard.outbox.initial-backoff-ms:5000}") long initialBackoffMs,
            @Value("${streamguard.outbox.max-backoff-ms:300000}") long maxBackoffMs) {

        this.outboxEventRepository = outboxEventRepository;
        this.paymentEventProducer = paymentEventProducer;
        this.objectMapper = objectMapper;
        this.maxRetries = maxRetries;
        this.initialBackoffMs = initialBackoffMs;
        this.maxBackoffMs = maxBackoffMs;
    }

    @Scheduled(fixedDelayString = "${streamguard.outbox.poll-interval-ms:5000}")
    public void publishPendingEvents() {

        List<OutboxEvent> pendingEvents =
                outboxEventRepository.findDueEvents("PENDING", LocalDateTime.now());

        for (OutboxEvent outboxEvent : pendingEvents) {

            LocalDateTime attemptedAt = LocalDateTime.now();
            outboxEvent.setLastAttemptedAt(attemptedAt);

            try {
                PaymentEvent event = objectMapper.readValue(
                        outboxEvent.getPayload(),
                        PaymentEvent.class
                );

                paymentEventProducer
                        .publishPaymentEvent(event)
                        .get(10, TimeUnit.SECONDS);

                outboxEvent.setStatus("PUBLISHED");
                outboxEvent.setPublishedAt(attemptedAt);
                outboxEvent.setLastError(null);
                outboxEvent.setNextAttemptAt(null);

                outboxEventRepository.save(outboxEvent);

                System.out.println(
                        "Outbox event published: " + outboxEvent.getId()
                );

            } catch (Exception exception) {

                int retryCount = outboxEvent.getRetryCount() + 1;
                outboxEvent.setRetryCount(retryCount);
                outboxEvent.setLastError(trimError(exception));

                if (retryCount >= maxRetries) {
                    outboxEvent.setStatus("FAILED");
                    outboxEvent.setNextAttemptAt(null);
                    System.err.println(
                            "Outbox event failed after retries: " + outboxEvent.getId()
                    );
                } else {
                    outboxEvent.setStatus("PENDING");
                    outboxEvent.setNextAttemptAt(
                            attemptedAt.plusNanos(backoffNanos(retryCount))
                    );
                    System.err.println(
                            "Failed to publish outbox event: "
                                    + outboxEvent.getId()
                                    + " (retry " + retryCount + "/" + maxRetries + ")"
                    );
                }

                outboxEventRepository.save(outboxEvent);
                exception.printStackTrace();
            }
        }
    }

    private long backoffNanos(int retryCount) {
        long multiplier = 1L << Math.min(retryCount - 1, 16);
        long delayMs = Math.min(initialBackoffMs * multiplier, maxBackoffMs);
        return delayMs * 1_000_000L;
    }

    private String trimError(Exception exception) {
        String message = exception.getClass().getSimpleName();
        if (exception.getMessage() != null && !exception.getMessage().isBlank()) {
            message = message + ": " + exception.getMessage();
        }
        if (message.length() > 1000) {
            return message.substring(0, 1000);
        }
        return message;
    }
}
