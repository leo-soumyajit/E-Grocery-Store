package com.soumyajit.E_Grocery.Shop.Entities;

import jakarta.persistence.*;
import lombok.*;

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
    private double totalQuantity;   // = unitQuantity * quantity
    private double totalPrice;      // = unitPrice * quantity
}
