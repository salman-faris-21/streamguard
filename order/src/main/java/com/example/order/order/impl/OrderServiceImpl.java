package com.example.order.order.impl;

import com.example.order.order.Order;
import com.example.order.order.OrderRepository;
import com.example.order.order.OrderService;
import com.example.order.order.dto.OrderRequestDTO;
import com.example.order.order.dto.OrderResponseDTO;

import com.example.order.order.enums.OrderStatus;

import com.example.order.order.mapper.OrderMapper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO request) {

        Order order = OrderMapper.toEntity(request);

        Order savedOrder = orderRepository.save(order);

        return OrderMapper.toResponseDTO(savedOrder);
    }

    @Override
    public OrderResponseDTO getOrderById(UUID orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return OrderMapper.toResponseDTO(order);
    }
    @Override
    public void updateOrderStatus(UUID orderId, OrderStatus status) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(status);

        orderRepository.save(order);
    }
}