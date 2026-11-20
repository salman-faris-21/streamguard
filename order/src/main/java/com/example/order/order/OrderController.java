package com.example.order.order;

import com.example.order.order.dto.OrderRequestDTO;
import com.example.order.order.dto.OrderResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    public static final String USER_ID_HEADER = "X-User-Id";

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody OrderRequestDTO request,
            @RequestHeader(value = USER_ID_HEADER, required = false) String customerId) {

        OrderResponseDTO response = orderService.createOrder(request, customerId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @PathVariable UUID orderId,
            @RequestHeader(value = USER_ID_HEADER, required = false) String customerId) {

        OrderResponseDTO response = orderService.getOrderById(orderId, customerId);

        return ResponseEntity.ok(response);
    }
}
