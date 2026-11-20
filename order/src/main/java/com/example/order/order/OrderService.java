package com.example.order.order;


import com.example.order.order.dto.OrderRequestDTO;
import com.example.order.order.dto.OrderResponseDTO;
import com.example.order.order.enums.OrderStatus;

import java.util.UUID;

public interface OrderService {

    OrderResponseDTO createOrder(OrderRequestDTO request, String customerId);
    OrderResponseDTO getOrderById(UUID orderId, String customerId);
    void updateOrderStatus(UUID orderId, OrderStatus status);
}
