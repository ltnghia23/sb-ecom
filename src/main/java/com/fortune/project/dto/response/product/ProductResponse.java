package com.fortune.project.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long productId;
    private String productName;
    private String productDescription;
    private String productImg;
    private Integer productQuantity;
    private double productPrice;
    private double productSpecialPrice;
}
