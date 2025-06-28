package com.soumyajit.E_Grocery.Shop.DTOS;

import com.soumyajit.E_Grocery.Shop.Entities.Address;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Setter
@Builder
public class OrderDTO {

    private Long orderId;
    private String customerName;
    private String customerEmail;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime placedAt;
    private List<OrderItemDTO> items;
    private List<Address> addresses;



}
