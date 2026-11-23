package com.example.payment.payment.event;

import com.example.payment.payment.event.PaymentEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dlt")
public class DeadLetterReplayController {

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    public DeadLetterReplayController(KafkaTemplate<Object, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/replay")
    public String replayEvent(@RequestBody PaymentEvent event) {

        kafkaTemplate.send(
                "payment-events",
                event.getOrderId().toString(),
                event
        );

        return "Payment event submitted for replay";
    }
}