package com.example.order.order.event;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "processed_payment_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_processed_payment_id",
                        columnNames = "payment_id"
                )
        }
)
public class ProcessedPaymentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false, unique = true)
    private UUID paymentId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    private LocalDateTime processedAt;

    public ProcessedPaymentEvent() {
    }

    public ProcessedPaymentEvent(UUID paymentId, UUID orderId) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.processedAt = LocalDateTime.now();
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
