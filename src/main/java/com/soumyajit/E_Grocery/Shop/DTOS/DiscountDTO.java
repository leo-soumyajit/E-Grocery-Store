package com.soumyajit.E_Grocery.Shop.DTOS;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Data
@Getter
@Setter
public class DiscountDTO {
    private BigDecimal discountPercentage; // e.g., 15 for 15%
    private long durationInHours; // e.g., 48 for 2 days
}
