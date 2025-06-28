package com.soumyajit.E_Grocery.Shop.DTOS;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressDTO {
    private String houseNumber;
    private String street;
    private String city;
    private String state;
    private String pinCode;
    private String country;
}

