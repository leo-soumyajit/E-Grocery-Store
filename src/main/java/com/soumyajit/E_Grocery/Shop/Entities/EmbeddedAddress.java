package com.soumyajit.E_Grocery.Shop.Entities;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmbeddedAddress {
    private String houseNumber;
    private String street;
    private String city;
    private String district;
    private String state;
    private String pinCode;
    private String country;
}
