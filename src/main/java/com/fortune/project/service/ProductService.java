package com.fortune.project.service;

import com.fortune.project.dto.request.product.ProductRequest;
import com.fortune.project.dto.response.common.ApiResponse;
import com.fortune.project.dto.response.common.PagingResponse;
import com.fortune.project.dto.response.product.ProductResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {

    ApiResponse<ProductResponse> createProduct(Long categoryId, ProductRequest request);

    ApiResponse<PagingResponse<ProductResponse>>  getAllProducts(Pageable pageable);

    ApiResponse<PagingResponse<ProductResponse>> getAllProductsByCategoryId(Long categoryId, Pageable pageable);

    ApiResponse<PagingResponse<ProductResponse>> getAllProductsByKeyword(String keyword, Pageable pageable);

    ApiResponse<ProductResponse> updateProductByProductId(Long productId, ProductRequest request);

    ApiResponse<Void> deleteProductById(Long productId);

    ApiResponse<ProductResponse> updateProductImage(long productId, MultipartFile image) throws IOException;
}
