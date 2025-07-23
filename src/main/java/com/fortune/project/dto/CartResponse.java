package com.fortune.project.dto;

import com.fortune.project.dto.response.product.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse <T>{
    private Long id;
    private Long userId;
    private T items;
    private Double totalPrice;
}
