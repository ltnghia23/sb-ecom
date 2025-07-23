package com.fortune.project.dto.request.order;

import com.fortune.project.dto.request.address.AddressCreateRequest;
import com.fortune.project.entity.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateOrderRequest {
    @NotEmpty(message = "Item list cannot be empty")
    private List<OrderItemRequest> items;

    //user chọn địa chỉ đã lưu
    private Long shippingAddressId;

    @NotNull(message = "Payment method cannot be null")
    private PaymentMethod paymentMethod;

    private String note;
}
