package com.soumyajit.E_Grocery.Shop.DTOS;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;
    private String name;
    private double unitQuantity;      // Example: 100
    private String unitLabel;         // Example: "gm"
    private double unitPrice;         // Example: 20.0
    private String imageUrl;
    private String description;
}

