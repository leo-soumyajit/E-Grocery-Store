package com.soumyajit.E_Grocery.Shop.DTOS;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserDTOS {
    private Long id;
    private String email;
    private String name;
    private String mob_no;
}
