package com.example.payment.payment.event;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByStatus(String status);

    @Query("""
            select e from OutboxEvent e
            where e.status = :status
              and (e.nextAttemptAt is null or e.nextAttemptAt <= :now)
            """)
    List<OutboxEvent> findDueEvents(
            @Param("status") String status,
            @Param("now") LocalDateTime now);
}
