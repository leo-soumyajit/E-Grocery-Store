package com.soumyajit.E_Grocery.Shop.DTOS;

import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class LoginDTOS {

    @Email
    private String email;
    private String password;
}
