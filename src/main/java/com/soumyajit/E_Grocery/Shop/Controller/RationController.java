package com.soumyajit.E_Grocery.Shop.Controller;

import com.soumyajit.E_Grocery.Shop.Advices.ApiResponse;
import com.soumyajit.E_Grocery.Shop.DTOS.RationListDTO;
import com.soumyajit.E_Grocery.Shop.Service.RationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/ration")
@RequiredArgsConstructor
public class RationController {
    private final RationService rationService;

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<String>> save(@AuthenticationPrincipal UserDetails ud, @RequestBody RationListDTO dto) {
        rationService.saveRationList(ud.getUsername(), dto);
        ApiResponse response = new ApiResponse("Ration list saved");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/checkout")
    public ResponseEntity<ApiResponse<String>> checkout(@RequestParam Long listId) {
        String message = rationService.checkoutRationList(listId);
        ApiResponse<String> response = new ApiResponse<>(message);
        return ResponseEntity.ok(response);
    }

}

