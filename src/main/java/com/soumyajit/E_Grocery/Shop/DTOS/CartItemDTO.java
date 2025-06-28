package com.soumyajit.E_Grocery.Shop.DTOS;

import lombok.Data;

@Data
public class CartItemDTO {
    private Long id;
    private Long productId;
    private int quantity;
    private double totalQuantity;
    private double totalPrice;
    private String unitLabel;
}
