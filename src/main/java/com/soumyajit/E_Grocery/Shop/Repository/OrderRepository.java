package com.soumyajit.E_Grocery.Shop.Repository;

import com.soumyajit.E_Grocery.Shop.Entities.OrderEntity;
import com.soumyajit.E_Grocery.Shop.Entities.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByCustomerId(Long customerId);
    List<OrderEntity> findAllByStatusInOrderByPlacedAtDesc(List<OrderStatus> statuses);

}
