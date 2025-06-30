package com.soumyajit.E_Grocery.Shop.DTOS;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class UserUpdateRequestDTO {
    private String name;
    private String mob_no;
}
