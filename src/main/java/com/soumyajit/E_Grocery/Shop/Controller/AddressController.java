package com.soumyajit.E_Grocery.Shop.Controller;

import com.soumyajit.E_Grocery.Shop.Advices.ApiResponse;
import com.soumyajit.E_Grocery.Shop.DTOS.AddressDTO;
import com.soumyajit.E_Grocery.Shop.Entities.Address;
import com.soumyajit.E_Grocery.Shop.Entities.User;
import com.soumyajit.E_Grocery.Shop.Exception.ResourceNotFound;
import com.soumyajit.E_Grocery.Shop.Repository.AddressRepository;
import com.soumyajit.E_Grocery.Shop.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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
                .district(dto.getDistrict())
                .state(dto.getState())
                .pinCode(dto.getPinCode())
                .country(dto.getCountry())
                .isActive(false)
                .user(user) // ✅ Setting the user here!
                .build();

        addressRepo.save(address);

        return ResponseEntity.ok(new ApiResponse<>("Address added successfully"));
    }


    @PutMapping("/update/{addressId}")
    public ResponseEntity<ApiResponse<String>> updateAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long addressId,
            @RequestBody AddressDTO dto) {

        User user = userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = addressRepo.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(new ApiResponse<>("Unauthorized to update this address"));
        }

        address.setUser(user);
        address.setHouseNumber(dto.getHouseNumber());
        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setState(dto.getState());
        address.setPinCode(dto.getPinCode());
        address.setCountry(dto.getCountry());

        addressRepo.save(address);

        return ResponseEntity.ok(new ApiResponse<>("Address updated successfully"));
    }


    @DeleteMapping("/delete/{addressId}")
    public ResponseEntity<ApiResponse<String>> deleteAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long addressId) {

        User user = userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = addressRepo.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(new ApiResponse<>("Unauthorized to delete this address"));
        }

        addressRepo.delete(address);

        return ResponseEntity.ok(new ApiResponse<>("Address deleted successfully"));
    }

    @PutMapping("/set-active/{addressId}")
    public ResponseEntity<ApiResponse<String>> setActiveAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long addressId) {

        User user = userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address newActive = addressRepo.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!newActive.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(new ApiResponse<>("Unauthorized to update this address"));
        }

        // Deactivate other addresses
        user.getAddresses().forEach(addr -> {
            if (Boolean.TRUE.equals(addr.getIsActive())) {
                addr.setIsActive(false);
                addressRepo.save(addr);
            }

        });

        // Activate selected address
        newActive.setIsActive(true);
        addressRepo.save(newActive);

        return ResponseEntity.ok(new ApiResponse<>("Address set as active for delivery"));
    }


    @GetMapping("/me")
    public ResponseEntity<List<Address>> getMyAddresses() {
        // Extract username/email from security context
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Fetch user
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Get all addresses of this user
        List<Address> addresses = addressRepo.findByUserId(user.getId());

        return ResponseEntity.ok(addresses);
    }











}
