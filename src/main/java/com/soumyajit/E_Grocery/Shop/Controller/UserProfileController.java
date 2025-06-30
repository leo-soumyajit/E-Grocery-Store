package com.soumyajit.E_Grocery.Shop.Controller;

import com.soumyajit.E_Grocery.Shop.Advices.ApiResponse;
import com.soumyajit.E_Grocery.Shop.DTOS.UserProfileDTOS;
import com.soumyajit.E_Grocery.Shop.DTOS.UserUpdateRequestDTO;
import com.soumyajit.E_Grocery.Shop.Service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user-profile")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;



    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTOS> getCurrentUserProfile() {
        UserProfileDTOS profile = userProfileService.getCurrentUserProfile();
        return ResponseEntity.ok(profile);
    }



    @PutMapping("/update")
    public ResponseEntity<ApiResponse<String>> updateUser(@RequestBody UserUpdateRequestDTO dto) {
        userProfileService.updateUserProfile(dto);
        return ResponseEntity.ok(new ApiResponse<>("✅ Profile updated successfully"));
    }



}














