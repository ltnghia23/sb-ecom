package com.fortune.project.controller;

import com.fortune.project.dto.request.order.CreateOrderRequest;
import com.fortune.project.dto.request.order.OrderResponse;
import com.fortune.project.security.service.UserDetailsImpl;
import com.fortune.project.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * API tạo đơn hàng + thanh toán
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * API lấy danh sách đơn hàng của user
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<OrderResponse> orders = orderService.getOrdersForCurrentUser();
        return ResponseEntity.ok(orders);
    }

    /**
     * API lấy chi tiết 1 đơn hàng
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetail(@PathVariable Long orderId) {
        OrderResponse response = orderService.getOrderDetail(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * API hủy đơn hàng
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}

