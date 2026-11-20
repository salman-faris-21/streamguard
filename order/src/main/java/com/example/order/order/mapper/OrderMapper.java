package com.example.order.order.mapper;


import com.example.order.order.Order;
import com.example.order.order.dto.OrderRequestDTO;
import com.example.order.order.dto.OrderResponseDTO;

public class OrderMapper {

    // Request DTO -> Entity
    public static Order toEntity(OrderRequestDTO request) {

        Order order = new Order();

        order.setCustomerId(request.getCustomerId());
        order.setNumberOfItems(request.getNumberOfItems());
        order.setTotalAmount(request.getAmount());
        order.setCurrency(request.getCurrency());

        return order;
    }

    // Entity -> Response DTO
    public static OrderResponseDTO toResponseDTO(Order order) {

        OrderResponseDTO response = new OrderResponseDTO();

        response.setId(order.getId());
        response.setCustomerId(order.getCustomerId());
        response.setNumberOfItems(order.getNumberOfItems());
        response.setTotalAmount(order.getTotalAmount());
        response.setCurrency(order.getCurrency());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        return response;
    }
}