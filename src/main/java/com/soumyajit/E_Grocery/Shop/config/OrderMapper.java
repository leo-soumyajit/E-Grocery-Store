package com.soumyajit.E_Grocery.Shop.config;

import com.soumyajit.E_Grocery.Shop.DTOS.OrderDTO;
import com.soumyajit.E_Grocery.Shop.DTOS.OrderItemDTO;
import com.soumyajit.E_Grocery.Shop.Entities.OrderEntity;
import com.soumyajit.E_Grocery.Shop.Entities.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderDTO toDto(OrderEntity order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .orderId(order.getId())
                .customerName(order.getCustomer().getName())
                .customerEmail(order.getCustomer().getEmail())
                .status(order.getStatus().name())
                .placedAt(order.getPlacedAt())
                .totalAmount(order.getTotalAmount())
                .items(itemDTOs)
                .deliveryAddress(order.getDeliveryAddress()) // ✅ Corrected
                .build();

    }


    public OrderItemDTO toDto(OrderItem item) {
        return OrderItemDTO.builder()
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .weight(item.getWeight())
                .build();
    }
}
