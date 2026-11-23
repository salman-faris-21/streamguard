package com.example.order.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class OrderRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldSaveAndRetrieveOrder() {

        Order order = new Order();
        order.setCustomerId("customer-123");
        order.setNumberOfItems(2);
        order.setTotalAmount(new BigDecimal("4999.00"));
        order.setCurrency("INR");

        Order savedOrder = orderRepository.save(order);

        Order retrievedOrder = orderRepository.findById(savedOrder.getId())
                .orElseThrow();

        assertNotNull(retrievedOrder.getId());
        assertEquals("customer-123", retrievedOrder.getCustomerId());
        assertEquals(2, retrievedOrder.getNumberOfItems());
        assertEquals(new BigDecimal("4999.00"), retrievedOrder.getTotalAmount());
        assertEquals("INR", retrievedOrder.getCurrency());
    }
}