package com.soumyajit.E_Grocery.Shop.Controller;

import com.soumyajit.E_Grocery.Shop.DTOS.CartItemDTO;
import com.soumyajit.E_Grocery.Shop.Entities.User;
import com.soumyajit.E_Grocery.Shop.Service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartItemDTO> addToCart(@RequestBody CartItemDTO dto) {
        Long customerId = getCurrentUserId();
        return ResponseEntity.ok(cartService.addToCart(customerId, dto));
    }

    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getCart() {
        Long customerId = getCurrentUserId();
        return ResponseEntity.ok(cartService.getCartItems(customerId));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long productId) {
        Long customerId = getCurrentUserId();
        cartService.removeFromCart(customerId, productId);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }
}
