package com.fortune.project.service.impl;

import com.fortune.project.dto.request.order.CreateOrderRequest;
import com.fortune.project.dto.request.order.OrderItemRequest;
import com.fortune.project.dto.request.order.OrderResponse;
import com.fortune.project.dto.request.order.PaymentResponse;
import com.fortune.project.entity.*;
import com.fortune.project.exception.ApiException;
import com.fortune.project.exception.ResourceNotFoundException;
import com.fortune.project.repository.OrderRepository;
import com.fortune.project.service.*;
import com.fortune.project.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final ProductService productService;
    private final AddressService addressService;
    private final UserService userService;
    private final AuthUtil authUtil;
    private final ShipmentService shipmentService;

    public OrderResponse createOrder(CreateOrderRequest request) {
        // Get current user
        UserEntity user = authUtil.loggedInUser();

        // Validate product and calculate total amount
        Double subtotal = calculateSubtotal(request.getItems());
        Double shippingFee = shipmentService.calculateShippingFee(request.getShippingAddressId(), subtotal);
        Double total = subtotal + shippingFee;

        // Create OrderEntity
        OrderEntity order = new OrderEntity();
        order.setCustomer(user);
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setTotal(total);
        order.setNote(request.getNote());

        // Gán địa chỉ giao hàng
        AddressEntity address = addressService.findById(request.getShippingAddressId());
        order.setAddress(address);

        // Tạo OrderItemEntity
        List<OrderItemEntity> items = productService.buildOrderItems(request.getItems(), order);
        order.setItems(items);

        // Lưu order
        orderRepository.save(order);

        // Tạo PaymentEntity
        PaymentResponse paymentResponse = paymentService.createPayment(order, request.getPaymentMethod());

        return OrderResponse.from(order, Collections.singletonList(paymentResponse));
    }

    public List<OrderResponse> getOrdersForCurrentUser() {
        UserEntity user = authUtil.loggedInUser();
        List<OrderEntity> orders = orderRepository.findByCustomer(user);
        return orders.stream().map(OrderResponse::from).toList();
    }

    public OrderResponse getOrderDetail(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "order id", orderId));
        List<PaymentResponse> paymentResponses = order.getPayments().stream()
                .map(PaymentResponse::from).toList();
        return OrderResponse.from(order, paymentResponses);
    }

    public void cancelOrder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "order id", orderId));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ApiException("Only pending orders can be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }


    private Double calculateSubtotal(List<OrderItemRequest> items) {
        return items.stream()
                .mapToDouble(item -> {
                    ProductEntity product = productService.findById(item.getProductId());
                    return product.getPrice() * item.getQuantity();
                })
                .sum();
    }
}
