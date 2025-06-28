package com.soumyajit.E_Grocery.Shop.Service;

import com.soumyajit.E_Grocery.Shop.DTOS.CartItemDTO;

import java.util.List;

public interface CartService {
    CartItemDTO addToCart(Long userId, CartItemDTO dto);
    List<CartItemDTO> getCartItems(Long userId);
    void removeFromCart(Long userId, Long productId);
}

