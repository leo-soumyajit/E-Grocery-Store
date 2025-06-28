package com.soumyajit.E_Grocery.Shop.DTOS;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminOrderResponseDTO {
    private Long orderId;
    private String customerName;
    private String mob_no;
    private String customerEmail;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime placedAt;
    private List<OrderItemDTO> items;
    private AddressDTO addresses;
}
