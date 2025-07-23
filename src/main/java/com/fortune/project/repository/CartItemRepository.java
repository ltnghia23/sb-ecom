package com.fortune.project.repository;

import com.fortune.project.entity.CartItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    CartItemEntity findByProduct_idAndCart_id(Long id, Long id1);

    Page<CartItemEntity> findByCart_id(Long id, Pageable pageable);
}
