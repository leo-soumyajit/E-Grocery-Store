package com.soumyajit.E_Grocery.Shop.Controller;

import com.soumyajit.E_Grocery.Shop.Advices.ApiResponse;
import com.soumyajit.E_Grocery.Shop.DTOS.OrderStatusUpdateDTO;
import com.soumyajit.E_Grocery.Shop.Entities.OrderStatus;
import com.soumyajit.E_Grocery.Shop.Entities.User;
import com.soumyajit.E_Grocery.Shop.Service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping("/place")
    public ResponseEntity<ApiResponse<String>> placeOrder() {
        Long customerId = getCurrentUserId();
        service.placeOrder(customerId);
        ApiResponse<String> response = new ApiResponse<>("Order placed successfully");
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<String>> updateStatus(@PathVariable Long orderId,
                                                            @RequestBody OrderStatusUpdateDTO dto) {
        String statusStr = dto.getStatus();
        service.updateStatus(orderId, statusStr);

        String message;
        switch (statusStr.toUpperCase()) {
            case "ACCEPTED":
                message = "Order accepted and will be delivered shortly";
                break;
            case "DELIVERED":
                message = "Order delivered";
                break;
            default:
                message = "Order status updated";
        }

        ApiResponse<String> response = new ApiResponse<>(message);
        return ResponseEntity.ok(response);
    }



    @GetMapping("/my")
    public ResponseEntity<?> getCustomerOrders() {
        Long customerId = getCurrentUserId();
        return ResponseEntity.ok(service.getOrdersByCustomer(customerId));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(service.getAllOrders());
    }

    private Long getCurrentUserId() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }
}
