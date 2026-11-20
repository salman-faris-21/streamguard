package com.example.order.order.event;



import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedPaymentEventRepository
        extends JpaRepository<ProcessedPaymentEvent, Long> {

    boolean existsByPaymentId(UUID paymentId);
}