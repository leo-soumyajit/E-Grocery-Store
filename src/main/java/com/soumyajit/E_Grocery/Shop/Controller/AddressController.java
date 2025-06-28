package com.soumyajit.E_Grocery.Shop.Controller;

import com.soumyajit.E_Grocery.Shop.Advices.ApiResponse;
import com.soumyajit.E_Grocery.Shop.DTOS.AddressDTO;
import com.soumyajit.E_Grocery.Shop.Entities.Address;
import com.soumyajit.E_Grocery.Shop.Entities.User;
import com.soumyajit.E_Grocery.Shop.Repository.AddressRepository;
import com.soumyajit.E_Grocery.Shop.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressRepository addressRepo;
    private final UserRepository userRepo;

    @PostMapping("/add")
    @Transactional
    public ResponseEntity<ApiResponse<String>> addAddressToLoggedInUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AddressDTO dto) {

        User user = userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = Address.builder()
                .houseNumber(dto.getHouseNumber())
                .street(dto.getStreet())
                .city(dto.getCity())
                .state(dto.getState())
                .pinCode(dto.getPinCode())
                .country(dto.getCountry())
                .build();

        if (user.getAddresses() == null) {
            user.setAddresses(new ArrayList<>());
        }

        user.getAddresses().add(address);
        userRepo.save(user);

        return ResponseEntity.ok(new ApiResponse<>("Address added successfully"));
    }
}
