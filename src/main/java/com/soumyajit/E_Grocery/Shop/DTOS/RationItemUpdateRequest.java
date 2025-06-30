package com.soumyajit.E_Grocery.Shop.DTOS;

// RationItemUpdateRequest.java

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RationItemUpdateRequest {
    private Long productId;
    private Integer quantity;
}

