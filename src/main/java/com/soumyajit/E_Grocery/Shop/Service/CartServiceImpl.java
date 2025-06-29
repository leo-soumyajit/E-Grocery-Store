package com.soumyajit.E_Grocery.Shop.Service;

import com.soumyajit.E_Grocery.Shop.DTOS.CartItemDTO;
import com.soumyajit.E_Grocery.Shop.Entities.CartItem;
import com.soumyajit.E_Grocery.Shop.Entities.Product;
import com.soumyajit.E_Grocery.Shop.Entities.User;
import com.soumyajit.E_Grocery.Shop.Exception.ResourceNotFound;
import com.soumyajit.E_Grocery.Shop.Repository.CartRepository;
import com.soumyajit.E_Grocery.Shop.Repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    public CartItemDTO addToCart(Long userId, CartItemDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFound("Product not found"));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        CartItem cartItem = cartRepository.findByCustomerIdAndProductId(user.getId(), product.getId())
                .orElse(new CartItem());

        cartItem.setCustomer(user);
        cartItem.setProduct(product);

        int updatedQuantity = cartItem.getQuantity() + dto.getQuantity();
        cartItem.setQuantity(updatedQuantity);

        // ✅ Determine effective unit price
        BigDecimal effectivePrice = getEffectivePrice(product);

        // ✅ Calculate totals
        cartItem.setTotalPrice(effectivePrice.multiply(BigDecimal.valueOf(updatedQuantity)));
        cartItem.setTotalQuantity(product.getUnitQuantity().multiply(BigDecimal.valueOf(updatedQuantity)));

        CartItem savedItem = cartRepository.save(cartItem);
        CartItemDTO response = modelMapper.map(savedItem, CartItemDTO.class);
        response.setUnitPrice(effectivePrice); // show price used
        return response;
    }




    @Override
    public List<CartItemDTO> getCartItems(Long userId) {
        return cartRepository.findByCustomerId(userId)
                .stream()
                .map(item -> {
                    CartItemDTO dto = new CartItemDTO();
                    dto.setId(item.getId());
                    dto.setProductId(item.getProduct().getId());
                    dto.setQuantity(item.getQuantity());
                    dto.setTotalPrice(item.getTotalPrice());
                    dto.setTotalQuantity(item.getTotalQuantity());
                    dto.setUnitLabel(item.getProduct().getUnitLabel());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void removeFromCart(Long userId, Long productId) {
        cartRepository.deleteByUserIdAndProductId(userId, productId);
    }

    private BigDecimal getEffectivePrice(Product product) {
        if (product.getDiscountedPrice() != null &&
                product.getDiscountExpiresAt() != null &&
                product.getDiscountExpiresAt().isAfter(LocalDateTime.now())) {
            return product.getDiscountedPrice();
        }
        return product.getUnitPrice();
    }



}
