package com.soumyajit.E_Grocery.Shop.DTOS;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class LoginResponseDTO {
    private Long id;
    private String accessToken;
    private String refreshToken;

    public LoginResponseDTO(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public LoginResponseDTO(Long id, String accessToken) {
        this.id = id;
        this.accessToken = accessToken;
    }

    public LoginResponseDTO(Long id,String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.id = id;
    }
}
