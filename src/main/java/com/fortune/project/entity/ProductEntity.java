package com.fortune.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(name = "name")
    private String productName;

    @Column(name = "description")
    private String productDescription;

    @Column(name = "stock_quantity")
    private Integer productQuantity;

    @Column(name = "price")
    private double productPrice;

    @Column(name = "special_price")
    private double productSpecialPrice;

    @Column(name = "image_url")
    private String productImg;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryEntity category;
}
