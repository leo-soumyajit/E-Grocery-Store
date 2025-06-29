package com.soumyajit.E_Grocery.Shop.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User customer;

    @ManyToOne
    private Product product;

    private int quantity;            // number of units added
    private BigDecimal totalQuantity;   // = unitQuantity * quantity
    private BigDecimal totalPrice;      // = unitPrice * quantity
}
