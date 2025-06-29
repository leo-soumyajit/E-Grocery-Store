package com.soumyajit.E_Grocery.Shop.DTOS;

import lombok.*;

@Data
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductDTOforUpdate {
    private Long id;
    private String name;
    private double unitQuantity;      // Example: 100
    private String unitLabel;         // Example: "gm"
    private double unitPrice;         // Example: 20.0
    private String description;
}
