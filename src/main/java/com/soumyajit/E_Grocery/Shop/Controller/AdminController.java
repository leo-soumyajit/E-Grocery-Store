package com.soumyajit.E_Grocery.Shop.Controller;

import com.soumyajit.E_Grocery.Shop.Advices.ApiResponse;
import com.soumyajit.E_Grocery.Shop.Service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/make-admin")
    public ResponseEntity<ApiResponse<String>> makeUserAdmin(@RequestParam String email) {
        adminService.makeUserAdmin(email);
        return ResponseEntity.ok(new ApiResponse<>("User promoted to ADMIN successfully 🚀"));
    }


}
