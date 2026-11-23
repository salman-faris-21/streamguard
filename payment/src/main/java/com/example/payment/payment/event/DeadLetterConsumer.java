package com.example.payment.payment.event;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterConsumer {

    @KafkaListener(
            topics = "payment-events.DLT",
            groupId = "order-service-dlt-group"
    )
    public void consumeDeadLetter(ConsumerRecord<String, PaymentEvent> record) {

        PaymentEvent event = record.value();

        String originalTopic = getHeader(record, "kafka_dlt-original-topic");
        String exceptionMessage = getHeader(record, "kafka_dlt-exception-message");
        String exceptionType = getHeader(record, "kafka_dlt-exception-fqcn");
        String stackTrace = getHeader(record, "kafka_dlt-exception-stacktrace");

        System.out.println("===== DEAD LETTER EVENT =====");
        System.out.println("Original Topic: " + originalTopic);
        System.out.println("Payment ID: " + event.getPaymentId());
        System.out.println("Order ID: " + event.getOrderId());
        System.out.println("Status: " + event.getStatus());
        System.out.println("Exception Type: " + exceptionType);
        System.out.println("Failure Reason: " + exceptionMessage);
        System.out.println("Stack Trace: " + stackTrace);
        System.out.println("============================");
    }

    private String getHeader(ConsumerRecord<String, PaymentEvent> record, String headerName) {
        Header header = record.headers().lastHeader(headerName);
        return header != null ? new String(header.value()) : "Not available";
    }
}