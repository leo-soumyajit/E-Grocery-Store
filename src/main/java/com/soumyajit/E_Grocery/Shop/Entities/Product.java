package com.soumyajit.E_Grocery.Shop.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
}

