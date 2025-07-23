package com.fortune.project.service;

import com.fortune.project.dto.response.common.ApiResponse;
import org.springframework.data.domain.Pageable;

public interface CartService {

    ApiResponse<?> addToCart(Long productId, Integer quantity, String email);

    ApiResponse<?> viewCart(Long id, Pageable pageable);

    ApiResponse<?> viewAllCart(Pageable pageable);
}
