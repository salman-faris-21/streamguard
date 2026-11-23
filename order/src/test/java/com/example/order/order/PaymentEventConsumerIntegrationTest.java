package com.example.order.order;

import com.example.order.order.Order;
import com.example.order.order.OrderRepository;
import com.example.order.order.enums.OrderStatus;
import com.example.order.order.event.PaymentEvent;
import com.example.order.order.event.ProcessedPaymentEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false"
})
@Testcontainers
class PaymentEventConsumerIntegrationTest {

    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    static KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.0"));

    static {
        postgres.start();
        kafka.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProcessedPaymentEventRepository eventRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldProcessPaymentEventAndMarkOrderPaid() throws Exception {

        // 1. Create an order
        Order order = new Order();
        order.setCustomerId("integration-test-user");
        order.setNumberOfItems(2);
        order.setTotalAmount(new BigDecimal("4999.00"));
        order.setCurrency("INR");
        order.setStatus(OrderStatus.PENDING);

        order = orderRepository.save(order);

        UUID orderId = order.getId();
        UUID paymentId = UUID.randomUUID();

        // 2. Create payment event
        PaymentEvent event = new PaymentEvent();
        event.setPaymentId(paymentId);
        event.setOrderId(orderId);
        event.setAmount(new BigDecimal("4999.00"));
        event.setCurrency("INR");
        event.setStatus("SUCCESS");

        // 3. Serialize event to JSON and publish to Kafka
        String eventJson = objectMapper.writeValueAsString(event);

        kafkaTemplate.send("payment-events", eventJson).get();

        // 4. Wait for Kafka consumer to process the event
        long timeout = System.currentTimeMillis() + 10000;

        Order updatedOrder = null;

        while (System.currentTimeMillis() < timeout) {

            updatedOrder = orderRepository.findById(orderId).orElse(null);

            boolean eventProcessed =
                    eventRepository.existsByPaymentId(paymentId);

            if (updatedOrder != null
                    && updatedOrder.getStatus() == OrderStatus.PAID
                    && eventProcessed) {
                break;
            }

            Thread.sleep(200);
        }

        // 5. Verify result
        assertNotNull(updatedOrder);

        assertEquals(
                OrderStatus.PAID,
                updatedOrder.getStatus(),
                "Order should be marked as PAID after consuming payment event"
        );

        assertTrue(
                eventRepository.existsByPaymentId(paymentId),
                "Payment event should be recorded as processed"
        );
    }
    @Test
    void shouldIgnoreDuplicatePaymentEvents() throws Exception {

        // 1. Create order
        Order order = new Order();
        order.setCustomerId("duplicate-test-user");
        order.setNumberOfItems(1);
        order.setTotalAmount(new BigDecimal("1000.00"));
        order.setCurrency("INR");
        order.setStatus(OrderStatus.PENDING);

        order = orderRepository.save(order);

        UUID orderId = order.getId();
        UUID paymentId = UUID.randomUUID();

        // 2. Create payment event
        PaymentEvent event = new PaymentEvent();
        event.setPaymentId(paymentId);
        event.setOrderId(orderId);
        event.setAmount(new BigDecimal("1000.00"));
        event.setCurrency("INR");
        event.setStatus("SUCCESS");

        String eventJson = objectMapper.writeValueAsString(event);

        // 3. Send the same event twice
        kafkaTemplate.send("payment-events", eventJson).get();
        kafkaTemplate.send("payment-events", eventJson).get();

        // 4. Wait for both messages to be processed
        long timeout = System.currentTimeMillis() + 10000;

        while (System.currentTimeMillis() < timeout) {

            if (eventRepository.countByPaymentId(paymentId) == 1
                    && orderRepository.findById(orderId)
                    .map(o -> o.getStatus() == OrderStatus.PAID)
                    .orElse(false)) {

                Thread.sleep(1000);
                break;
            }

            Thread.sleep(200);
        }

        // 5. Verify
        Order updatedOrder = orderRepository.findById(orderId).orElseThrow();

        assertEquals(OrderStatus.PAID, updatedOrder.getStatus());

        assertEquals(
                1,
                eventRepository.countByPaymentId(paymentId),
                "Duplicate payment events should not create duplicate processed records"
        );
    }
}