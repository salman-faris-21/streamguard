package com.example.order.order.event;

import com.example.order.order.OrderService;


import com.example.order.order.enums.OrderStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentEventConsumer {

    private final OrderService orderService;
    private final ProcessedPaymentEventRepository eventRepository;

    public PaymentEventConsumer(
            OrderService orderService,
            ProcessedPaymentEventRepository eventRepository) {
        this.orderService = orderService;
        this.eventRepository = eventRepository;
    }

    @KafkaListener(
            topics = "payment-events",
            groupId = "order-service-group"
    )
    @Transactional
    public void consumePaymentEvent(PaymentEvent event) {

        // 1. Check whether this event was already processed
        if (eventRepository.existsByPaymentId(event.getPaymentId())) {
            System.out.println(
                    "Duplicate event ignored: " + event.getPaymentId()
            );
            return;
        }

        // 2. Map payment status to order status
        OrderStatus orderStatus = switch (event.getStatus()) {
            case "SUCCESS" -> OrderStatus.PAID;
            case "FAILED" -> OrderStatus.CANCELLED;
            default -> throw new IllegalArgumentException(
                    "Unknown payment status: " + event.getStatus()
            );
        };

        // 3. Update order
        orderService.updateOrderStatus(
                event.getOrderId(),
                orderStatus
        );

        // 4. Record the processed event
        ProcessedPaymentEvent processedEvent =
                new ProcessedPaymentEvent(
                        event.getPaymentId(),
                        event.getOrderId()
                );

        eventRepository.save(processedEvent);

        System.out.println(
                "Order updated to " + orderStatus
                        + " for payment " + event.getPaymentId()
        );
    }
}