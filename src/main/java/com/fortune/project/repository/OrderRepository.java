package com.fortune.project.repository;

import com.fortune.project.entity.OrderEntity;
import com.fortune.project.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByCustomer(UserEntity user);
}
