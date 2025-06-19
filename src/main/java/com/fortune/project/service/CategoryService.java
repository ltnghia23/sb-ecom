package com.fortune.project.service;

import com.fortune.project.dto.request.CategoryCreateRequest;
import com.fortune.project.dto.request.CategoryUpdateRequest;
import com.fortune.project.dto.response.ApiResponse;
import com.fortune.project.dto.response.CategoryResponse;
import com.fortune.project.dto.response.PagingResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    ApiResponse<PagingResponse<CategoryResponse>> getAllCategories(Pageable pageable);
    ApiResponse<CategoryResponse> createCategory(CategoryCreateRequest category);
    ApiResponse<CategoryResponse> deleteCategory(Long id);
    ApiResponse<CategoryResponse> updateCategory(Long id, CategoryUpdateRequest category);
}
