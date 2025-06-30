package com.soumyajit.E_Grocery.Shop.DTOS;

import jakarta.persistence.Column;
import lombok.Data;

import java.util.List;

@Data
public class UserProfileDTOS {
    private Long id;
    private String name;

    private String email;

    @Column(nullable = false)
    private String mob_no;


}
