package com.fortune.project.dto.request.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductRequest {
    private Long productId;
    private String productName;
    @JsonProperty("description")
    private String productDescription;
    @JsonProperty("stock")
    private Integer stock;
    @JsonProperty("price")
    private double productPrice;
    @JsonProperty("discount")
    private double productDiscount;
}
