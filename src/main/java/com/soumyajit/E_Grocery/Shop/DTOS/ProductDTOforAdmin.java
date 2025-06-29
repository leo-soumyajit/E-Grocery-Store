package com.soumyajit.E_Grocery.Shop.DTOS;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductDTOforAdmin {
    private Long id;
    private String name;
    private BigDecimal unitQuantity;      // Example: 100
    private String unitLabel;         // Example: "gm"
    private BigDecimal unitPrice;         // Example: 20.0
    private String imageUrl;
    private String description;
    private boolean active;
    private BigDecimal discountPercentage; // Optional
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime discountExpiresAt; // Optional

}
