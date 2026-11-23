package com.example.order.order;

import com.example.order.order.dto.OrderRequestDTO;
import com.example.order.order.dto.OrderResponseDTO;
import com.example.order.order.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.UUID;


import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void shouldCreateOrderAndReturn201() throws Exception {

        UUID orderId = UUID.randomUUID();

        OrderResponseDTO response = new OrderResponseDTO();
        response.setId(orderId);
        response.setCustomerId("customer-123");
        response.setNumberOfItems(2);
        response.setTotalAmount(new BigDecimal("4999.00"));
        response.setCurrency("INR");
        response.setStatus(OrderStatus.PENDING);

        when(orderService.createOrder(any(OrderRequestDTO.class), eq("customer-123")))
                .thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .header("X-User-Id", "customer-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "numberOfItems": 2,
                                    "amount": 4999.00,
                                    "currency": "INR"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.customerId").value("customer-123"))
                .andExpect(jsonPath("$.numberOfItems").value(2))
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(orderService).createOrder(any(OrderRequestDTO.class), eq("customer-123"));
    }

    @Test
    void shouldGetOrderAndReturn200() throws Exception {

        UUID orderId = UUID.randomUUID();

        OrderResponseDTO response = new OrderResponseDTO();
        response.setId(orderId);
        response.setCustomerId("customer-123");
        response.setNumberOfItems(2);
        response.setTotalAmount(new BigDecimal("4999.00"));
        response.setCurrency("INR");
        response.setStatus(OrderStatus.PAID);

        when(orderService.getOrderById(orderId, "customer-123"))
                .thenReturn(response);

        mockMvc.perform(get("/api/orders/{orderId}", orderId)
                        .header("X-User-Id", "customer-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.customerId").value("customer-123"))
                .andExpect(jsonPath("$.status").value("PAID"));

        verify(orderService).getOrderById(orderId, "customer-123");
    }

    @Test
    void shouldRejectInvalidOrderRequest() throws Exception {

        mockMvc.perform(post("/api/orders")
                        .header("X-User-Id", "customer-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "numberOfItems": -1,
                                    "amount": -100,
                                    "currency": "IN"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldRejectInvalidOrderId() throws Exception {

        mockMvc.perform(get("/api/orders/invalid-id")
                        .header("X-User-Id", "customer-123"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }
}