package com.soumyajit.E_Grocery.Shop.Controller;

import com.soumyajit.E_Grocery.Shop.Advices.ApiResponse;
import com.soumyajit.E_Grocery.Shop.DTOS.RationItemDTO;
import com.soumyajit.E_Grocery.Shop.DTOS.RationItemUpdateRequest;
import com.soumyajit.E_Grocery.Shop.DTOS.RationListDTO;
import com.soumyajit.E_Grocery.Shop.Entities.RationList;
import com.soumyajit.E_Grocery.Shop.Entities.User;
import com.soumyajit.E_Grocery.Shop.Service.RationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/ration")
@RequiredArgsConstructor
@Slf4j
public class RationController {
    private final RationService rationService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addRationItem(
            @AuthenticationPrincipal User user,
            @RequestBody RationItemDTO dto) {
        rationService.addOrUpdateRationItem(user.getEmail(), dto);
        ApiResponse apiResponse = new ApiResponse("Product added in your ration list.");
        return ResponseEntity.ok(apiResponse);
    }


    @GetMapping("/checkout")
    public ResponseEntity<ApiResponse<String>> checkout(@AuthenticationPrincipal User user) {
        String message = rationService.checkoutRationList(user);
        return ResponseEntity.ok(new ApiResponse<>(message));
    }


    @GetMapping("/my")
    public ResponseEntity<RationListDTO> getMyRationList(@AuthenticationPrincipal User user) {
        RationListDTO dto = rationService.getMyRationList(user);
        return ResponseEntity.ok(dto);
    }


    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteMyRationList(@AuthenticationPrincipal User user) {
        rationService.deleteMyRationList(user);
        ApiResponse apiResponse = new ApiResponse("Ration list deleted successfully.");
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<ApiResponse<String>> deleteRationItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId) {
        log.info("Deleting ration item");
        rationService.deleteRationItem(user, productId);
        ApiResponse<String> apiResponse = new ApiResponse<>("Ration item deleted successfully.");
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<String>> updateRationItem(
            @AuthenticationPrincipal User user,
            @RequestBody RationItemUpdateRequest request) {
        rationService.updateRationItem(user, request);
        ApiResponse apiResponse = new ApiResponse("Ration item updated successfully.");
        return ResponseEntity.ok(apiResponse);
    }


}

