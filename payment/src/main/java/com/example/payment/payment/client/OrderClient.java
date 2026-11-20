package com.example.payment.payment.client;

import com.example.payment.payment.orderEntity.OrderResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "order")
public interface OrderClient {

    @GetMapping("/api/orders/{orderId}")
    OrderResponseDTO getOrderById(@PathVariable UUID orderId);
}
