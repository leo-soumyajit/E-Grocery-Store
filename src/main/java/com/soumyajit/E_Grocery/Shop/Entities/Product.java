package com.soumyajit.E_Grocery.Shop.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    private BigDecimal unitPrice;          // e.g., ₹20 per 100gm
    private BigDecimal unitQuantity;       // e.g., 100
    private String unitLabel;          // e.g., "gm", "kg", "ltr"

    private String imageUrl;           // Product image URL
    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "discount_percentage")
    private BigDecimal discountPercentage; // e.g., 15 for 15%

    @Column(name = "discounted_price")
    private BigDecimal discountedPrice;

    @Column(name = "discount_expires_at")
    private LocalDateTime discountExpiresAt;


}

