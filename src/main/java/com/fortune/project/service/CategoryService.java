package com.fortune.project.service;

import com.fortune.project.dto.request.CategoryCreateRequest;
import com.fortune.project.dto.request.CategoryUpdateRequest;
import com.fortune.project.dto.response.ApiResponse;
import com.fortune.project.dto.response.CategoryResponse;
import com.fortune.project.model.Category;

import java.util.List;

public interface CategoryService {
    ApiResponse<List<CategoryResponse>> getAllCategories();
    ApiResponse<CategoryResponse> createCategory(CategoryCreateRequest category);
    ApiResponse<Void> deleteCategory(Long id);
    ApiResponse<CategoryResponse> updateCategory(Long id, CategoryUpdateRequest category);
}
