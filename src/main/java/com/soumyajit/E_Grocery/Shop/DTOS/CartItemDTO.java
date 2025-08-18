package com.soumyajit.E_Grocery.Shop.DTOS;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String imageUrl;
    private int quantity;
    private BigDecimal totalQuantity;
    private BigDecimal totalPrice;
    private String unitLabel;
    private BigDecimal unitPrice; // this is dynamic price shown to user

}
