package com.example.order;

import com.example.order.order.OrderService;
import com.example.order.order.enums.OrderStatus;
import com.example.order.order.event.PaymentEvent;
import com.example.order.order.event.PaymentEventConsumer;
import com.example.order.order.event.ProcessedPaymentEvent;
import com.example.order.order.event.ProcessedPaymentEventRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventConsumerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private ProcessedPaymentEventRepository eventRepository;

    @InjectMocks
    private PaymentEventConsumer paymentEventConsumer;

    @Test
    void shouldMarkOrderAsPaidWhenPaymentIsSuccessful() {

        // Arrange
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(eventRepository.existsByPaymentId(paymentId))
                .thenReturn(false);

        PaymentEvent event = new PaymentEvent();
        event.setPaymentId(paymentId);
        event.setOrderId(orderId);
        event.setStatus("SUCCESS");

        // Act
        paymentEventConsumer.consumePaymentEvent(event);

        // Assert
        verify(orderService).updateOrderStatus(
                orderId,
                OrderStatus.PAID
        );

        verify(eventRepository).save(any(ProcessedPaymentEvent.class));
    }
    @Test
    void shouldIgnoreDuplicatePaymentEvent() {

        // Arrange
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(eventRepository.existsByPaymentId(paymentId))
                .thenReturn(true);

        PaymentEvent event = new PaymentEvent();
        event.setPaymentId(paymentId);
        event.setOrderId(orderId);
        event.setStatus("SUCCESS");

        // Act
        paymentEventConsumer.consumePaymentEvent(event);

        // Assert
        verify(orderService, never())
                .updateOrderStatus(any(UUID.class), any(OrderStatus.class));

        verify(eventRepository, never())
                .save(any(ProcessedPaymentEvent.class));
    }
    @Test
    void shouldCancelOrderWhenPaymentFails() {

        // Arrange
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(eventRepository.existsByPaymentId(paymentId))
                .thenReturn(false);

        PaymentEvent event = new PaymentEvent();
        event.setPaymentId(paymentId);
        event.setOrderId(orderId);
        event.setStatus("FAILED");

        // Act
        paymentEventConsumer.consumePaymentEvent(event);

        // Assert
        verify(orderService).updateOrderStatus(
                orderId,
                OrderStatus.CANCELLED
        );

        verify(eventRepository).save(any(ProcessedPaymentEvent.class));
    }
    @Test
    void shouldThrowExceptionWhenPaymentStatusIsUnknown() {

        // Arrange
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(eventRepository.existsByPaymentId(paymentId))
                .thenReturn(false);

        PaymentEvent event = new PaymentEvent();
        event.setPaymentId(paymentId);
        event.setOrderId(orderId);
        event.setStatus("UNKNOWN");

        // Act + Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentEventConsumer.consumePaymentEvent(event)
        );

        verify(orderService, never())
                .updateOrderStatus(any(UUID.class), any(OrderStatus.class));

        verify(eventRepository, never())
                .save(any(ProcessedPaymentEvent.class));
    }
}