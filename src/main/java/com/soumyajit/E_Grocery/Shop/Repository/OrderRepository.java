package com.soumyajit.E_Grocery.Shop.Repository;

import com.soumyajit.E_Grocery.Shop.Entities.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByCustomerId(Long customerId);
}
