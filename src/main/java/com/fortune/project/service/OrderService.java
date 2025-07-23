package com.fortune.project.service;

import com.fortune.project.dto.request.order.CreateOrderRequest;
import com.fortune.project.dto.request.order.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);

    List<OrderResponse> getOrdersForCurrentUser();

    OrderResponse getOrderDetail(Long orderId);

    void cancelOrder(Long orderId);
}
