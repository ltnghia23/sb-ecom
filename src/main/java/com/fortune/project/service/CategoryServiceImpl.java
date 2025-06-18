package com.fortune.project.service;

import com.fortune.project.dto.request.CategoryCreateRequest;
import com.fortune.project.dto.request.CategoryUpdateRequest;
import com.fortune.project.dto.response.ApiResponse;
import com.fortune.project.dto.response.CategoryResponse;
import com.fortune.project.exception.ApiException;
import com.fortune.project.exception.ResourceNotFoundException;
import com.fortune.project.model.Category;
import com.fortune.project.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    private CategoryRepository repo;

    @Autowired
    public CategoryServiceImpl(CategoryRepository repo) {
        this.repo = repo;
    }

    @Override
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        List<Category> categories = repo.findAll();
        if (categories.isEmpty()) throw new ApiException("Have no category created till now", "Categories is empty");
        List<CategoryResponse> responses = categories
                .stream()
                .map(c -> new CategoryResponse(c.getCategoryId(), c.getCategoryName()))
                .toList();
        return new ApiResponse<>("Categories fetched success", responses, LocalDateTime.now());
    }

    @Override
    public ApiResponse<CategoryResponse> createCategory(CategoryCreateRequest category) {
        Category checkedCategory = repo.findByCategoryName(category.getCategoryName());
        if (checkedCategory != null) {
            throw new ApiException("Category with name %s already exist!!"
                    .formatted(checkedCategory.getCategoryName()), "Duplicated category name");
        }
        Category createdCategory = new Category();
        createdCategory.setCategoryName(category.getCategoryName());
        Category saved = repo.save(createdCategory);
        return new ApiResponse<>("Category created",
                new CategoryResponse(saved.getCategoryId(), saved.getCategoryName()),
                LocalDateTime.now());
    }

    @Override
    public ApiResponse<Void> deleteCategory(Long id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("Category", "categoryId", id);
        repo.deleteById(id);
        return new ApiResponse<>("Category deleted successfully!", null, LocalDateTime.now());
    }

    @Override
    public ApiResponse<CategoryResponse> updateCategory(Long id, CategoryUpdateRequest category) {
        Category categoryToUpdate = repo.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Category", "categoryId", id));
        ;

        categoryToUpdate.setCategoryName(category.getCategoryName());
        Category updatedCategory = repo.save(categoryToUpdate);
        return new ApiResponse<>("Category updated successfully",
                new CategoryResponse(updatedCategory.getCategoryId(),
                        updatedCategory.getCategoryName()), LocalDateTime.now());
    }


}
