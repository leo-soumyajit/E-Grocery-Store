package com.soumyajit.E_Grocery.Shop.DTOS;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ProductDTO {
    private Long id;
    private String name;
    private BigDecimal unitQuantity;      // Example: 100
    private String unitLabel;         // Example: "gm"
    private BigDecimal unitPrice;         // Example: 20.0
    private String imageUrl;
    private String description;
    private BigDecimal discountPercentage;
    private LocalDateTime discountExpiresAt;

}
