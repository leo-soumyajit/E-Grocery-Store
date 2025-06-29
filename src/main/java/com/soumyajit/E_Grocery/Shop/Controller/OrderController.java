package com.soumyajit.E_Grocery.Shop.Controller;

import com.soumyajit.E_Grocery.Shop.Advices.ApiResponse;
import com.soumyajit.E_Grocery.Shop.DTOS.AdminOrderResponseDTO;
import com.soumyajit.E_Grocery.Shop.DTOS.OrderStatusUpdateDTO;
import com.soumyajit.E_Grocery.Shop.Entities.User;
import com.soumyajit.E_Grocery.Shop.Service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/place")
    public ResponseEntity<ApiResponse<String>> placeOrder() {
        Long customerId = getCurrentUserId();
        orderService.placeOrder(customerId);
        ApiResponse<String> response = new ApiResponse<>("Order placed successfully");
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<String>> updateStatus(@PathVariable Long orderId,
                                                            @RequestBody OrderStatusUpdateDTO dto) {
        String statusStr = dto.getStatus();
        orderService.updateStatus(orderId, statusStr);

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
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/admin/active")
    public ResponseEntity<?> getActiveOrders() {
        List<AdminOrderResponseDTO> activeOrders = orderService.getActiveOrders();
        return ResponseEntity.ok(new ApiResponse<>(activeOrders));
    }


    private Long getCurrentUserId() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }
    @DeleteMapping("/cancel/{orderId}")
    public ResponseEntity<ApiResponse> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(value = "reason", required = false) String reason
    ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            orderService.cancelOrder(orderId, email, reason);
            return ResponseEntity.ok(new ApiResponse("✅ Order cancelled successfully"));
        } catch (AccessDeniedException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse( "⛔ " + e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse( "⚠️ " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse( "❌ Something went wrong"));
        }
    }


}
