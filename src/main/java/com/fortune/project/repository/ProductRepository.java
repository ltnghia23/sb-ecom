package com.fortune.project.repository;


import com.fortune.project.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    Page<ProductEntity> findByCategory_id(Long categoryId, Pageable pageable);
    Page<ProductEntity> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}
