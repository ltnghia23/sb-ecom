package com.fortune.project.service;

import com.fortune.project.dto.request.CategoryCreateRequest;
import com.fortune.project.dto.request.CategoryUpdateRequest;
import com.fortune.project.dto.response.ApiResponse;
import com.fortune.project.dto.response.CategoryResponse;
import com.fortune.project.model.Category;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    private List<Category> categories = new ArrayList<>();
    private Long nextId = 1L;

    @Override
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> responses = categories
                .stream()
                .map(c -> new CategoryResponse(c.getCategoryId(), c.getCategoryName()))
                .toList();
        return new ApiResponse<>("Categories fetched success", responses, LocalDateTime.now());
    }

    @Override
    public ApiResponse<CategoryResponse> createCategory(CategoryCreateRequest category) {
        Category createdCategory = new Category();
        createdCategory.setCategoryId(nextId++);
        createdCategory.setCategoryName(category.getCategoryName());
        categories.add(createdCategory);
        return new ApiResponse<>("Category created",
                new CategoryResponse(createdCategory.getCategoryId(), createdCategory.getCategoryName()),
                LocalDateTime.now());
    }

    @Override
    public ApiResponse<Void> deleteCategory(Long id) {
        Category category = categories.stream()
                .filter(c -> c.getCategoryId().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Category not found"));
        categories.remove(category);
        return new ApiResponse<>("Category deleted successfully", null, LocalDateTime.now());
    }

    @Override
    public ApiResponse<CategoryResponse> updateCategory(Long id, CategoryUpdateRequest category) {
        Category updatedCategory = categories.stream()
                .filter(c -> c.getCategoryId().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Category not found for update"));
        updatedCategory.setCategoryName(category.getCategoryName());
        return new ApiResponse<>("Category updated successfully",
                new CategoryResponse(updatedCategory.getCategoryId(),
                        updatedCategory.getCategoryName()), LocalDateTime.now());
    }


}
