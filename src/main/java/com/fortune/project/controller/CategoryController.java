package com.fortune.project.controller;

import com.fortune.project.dto.request.CategoryCreateRequest;
import com.fortune.project.dto.response.ApiResponse;
import com.fortune.project.dto.response.CategoryResponse;
import com.fortune.project.model.Category;
import com.fortune.project.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/api/public/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories(){
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PostMapping("api/public/categories")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@RequestBody CategoryCreateRequest category){
        ApiResponse<CategoryResponse> response = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("api/admin/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("api/public/categories/{id}")
    public ResponseEntity<String> updateCategory(@RequestBody Category category,
                                                 @PathVariable Long id){
        categoryService.updateCategory(id, category);
        return  ResponseEntity.ok("Update category successfully");
    }

}
