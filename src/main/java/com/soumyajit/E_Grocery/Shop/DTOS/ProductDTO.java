package com.soumyajit.E_Grocery.Shop.DTOS;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder


@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDTO {
    private Long id;
    private String name;
    private BigDecimal unitQuantity;      // Example: 100
    private String unitLabel;         // Example: "gm"
    private BigDecimal unitPrice;         // Example: 20.0
    private String imageUrl;
    private String description;

    private BigDecimal discountPercentage;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime discountExpiresAt;

}
