package com.example.order.order;

import com.example.order.order.dto.OrderRequestDTO;
import com.example.order.order.dto.OrderResponseDTO;
import com.example.order.order.enums.OrderStatus;
import com.example.order.order.impl.OrderServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    // 1. Create order successfully
    @Test
    void shouldCreateOrderSuccessfully() {

        // Arrange
        String customerId = "customer-101";

        OrderRequestDTO request = new OrderRequestDTO();
        request.setNumberOfItems(2);
        request.setAmount(new BigDecimal("4999.00"));
        request.setCurrency("INR");

        Order savedOrder = new Order();
        savedOrder.setId(UUID.randomUUID());
        savedOrder.setCustomerId(customerId);
        savedOrder.setNumberOfItems(2);
        savedOrder.setTotalAmount(new BigDecimal("4999.00"));
        savedOrder.setCurrency("INR");
        savedOrder.setStatus(OrderStatus.PENDING);
        savedOrder.setCreatedAt(LocalDateTime.now());

        when(orderRepository.save(any(Order.class)))
                .thenReturn(savedOrder);

        // Act
        OrderResponseDTO response =
                orderService.createOrder(request, customerId);

        // Assert
        assertNotNull(response);
        assertEquals(savedOrder.getId(), response.getId());
        assertEquals(customerId, response.getCustomerId());
        assertEquals(2, response.getNumberOfItems());
        assertEquals(new BigDecimal("4999.00"), response.getTotalAmount());
        assertEquals("INR", response.getCurrency());

        verify(orderRepository).save(any(Order.class));
    }

    // 2. Reject order creation when customer ID is missing
    @Test
    void shouldRejectOrderCreationWhenCustomerIdIsMissing() {

        // Arrange
        OrderRequestDTO request = new OrderRequestDTO();
        request.setNumberOfItems(1);
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("INR");

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.createOrder(request, null)
        );

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());

        verify(orderRepository, never()).save(any(Order.class));
    }

    // 3. Retrieve order successfully for its owner
    @Test
    void shouldReturnOrderWhenCustomerOwnsOrder() {

        // Arrange
        UUID orderId = UUID.randomUUID();
        String customerId = "customer-101";

        Order order = createOrder(orderId, customerId);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        // Act
        OrderResponseDTO response =
                orderService.getOrderById(orderId, customerId);

        // Assert
        assertNotNull(response);
        assertEquals(orderId, response.getId());
        assertEquals(customerId, response.getCustomerId());

        verify(orderRepository).findById(orderId);
    }

    // 4. Reject retrieval when order does not exist
    @Test
    void shouldThrowExceptionWhenOrderDoesNotExist() {

        // Arrange
        UUID orderId = UUID.randomUUID();

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.getOrderById(orderId, "customer-101")
        );

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    // 5. Reject retrieval when customer does not own order
    @Test
    void shouldRejectAccessWhenCustomerDoesNotOwnOrder() {

        // Arrange
        UUID orderId = UUID.randomUUID();

        Order order = createOrder(orderId, "customer-101");

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.getOrderById(orderId, "customer-999")
        );

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    // 6. Reject retrieval when customer ID is missing
    @Test
    void shouldRejectRetrievalWhenCustomerIdIsMissing() {

        // Arrange
        UUID orderId = UUID.randomUUID();

        Order order = createOrder(orderId, "customer-101");

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.getOrderById(orderId, null)
        );

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    // 7. Update order status successfully
    @Test
    void shouldUpdateOrderStatusSuccessfully() {

        // Arrange
        UUID orderId = UUID.randomUUID();

        Order order = createOrder(orderId, "customer-101");

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        // Act
        orderService.updateOrderStatus(orderId, OrderStatus.PAID);

        // Assert
        assertEquals(OrderStatus.PAID, order.getStatus());

        verify(orderRepository).save(order);
    }

    // 8. Reject status update when order does not exist
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingOrder() {

        // Arrange
        UUID orderId = UUID.randomUUID();

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.updateOrderStatus(
                        orderId,
                        OrderStatus.PAID
                )
        );

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(orderRepository, never()).save(any(Order.class));
    }

    // Helper method
    private Order createOrder(UUID orderId, String customerId) {

        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(customerId);
        order.setNumberOfItems(2);
        order.setTotalAmount(new BigDecimal("4999.00"));
        order.setCurrency("INR");
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        return order;
    }
}