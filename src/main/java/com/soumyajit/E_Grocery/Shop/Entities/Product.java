package com.soumyajit.E_Grocery.Shop.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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

    private double unitPrice;          // e.g., ₹20 per 100gm
    private double unitQuantity;       // e.g., 100
    private String unitLabel;          // e.g., "gm", "kg", "ltr"

    private String imageUrl;           // Product image URL
    @Column(nullable = false)
    private boolean active = true;

}

