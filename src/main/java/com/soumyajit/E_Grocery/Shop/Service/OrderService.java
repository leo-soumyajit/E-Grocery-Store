package com.soumyajit.E_Grocery.Shop.Service;

import com.soumyajit.E_Grocery.Shop.DTOS.*;
import com.soumyajit.E_Grocery.Shop.EmailService.SendInvoiceEmailService;
import com.soumyajit.E_Grocery.Shop.Entities.*;
import com.soumyajit.E_Grocery.Shop.Exception.ResourceNotFound;
import com.soumyajit.E_Grocery.Shop.Repository.CartRepository;
import com.soumyajit.E_Grocery.Shop.Repository.OrderRepository;
import com.soumyajit.E_Grocery.Shop.Repository.ProductRepository;
import com.soumyajit.E_Grocery.Shop.Repository.UserRepository;
import com.soumyajit.E_Grocery.Shop.config.OrderMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepo;
    private final CartRepository cartRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final ModelMapper mapper;
    private final SendInvoiceEmailService sendInvoiceEmailService;
    private final OrderMapper orderMapper;


    @Transactional // ✅ Add this annotation
    public void placeOrder(Long customerId) {
        List<CartItem> cartItems = cartRepo.findByCustomerId(customerId);
        if (cartItems.isEmpty()) {
            throw new ResourceNotFound("Cart is empty");
        }

        User customer = userRepo.findById(customerId)
                .orElseThrow(() -> new ResourceNotFound("User not found"));

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cart : cartItems) {
            Product product = cart.getProduct();
            int quantity = cart.getQuantity();

            BigDecimal itemTotalPrice = BigDecimal.valueOf(product.getUnitPrice())
                    .multiply(BigDecimal.valueOf(quantity));
            totalAmount = totalAmount.add(itemTotalPrice);

            double totalUnitQty = product.getUnitQuantity() * quantity;
            String weight = totalUnitQty + product.getUnitLabel();

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(quantity)
                    .price(itemTotalPrice)
                    .weight(weight)
                    .build();

            orderItems.add(orderItem);
        }

        OrderEntity order = OrderEntity.builder()
                .customer(customer)
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .placedAt(LocalDateTime.now())
                .items(orderItems)
                .build();

        orderItems.forEach(item -> item.setOrder(order));

        orderRepo.save(order);
        cartRepo.deleteByCustomerId(customerId); // ✅ This requires transaction
    }


    public void updateStatus(Long orderId, String statusStr) {
        OrderEntity order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFound("Order not found"));

        OrderStatus status;
        try {
            status = OrderStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + statusStr);
        }

        order.setStatus(status);
        if (status == OrderStatus.ACCEPTED) {
            order.setAcceptedAt(LocalDateTime.now());
        }
        if (status == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());

            // ✅ Convert entity to DTO and send invoice
            OrderDTO orderDTO = orderMapper.toDto(order);
            sendInvoiceEmailService.sendInvoice(orderDTO);
        }

        orderRepo.save(order);
    }



    public List<OrderResponseDTO> getOrdersByCustomer(Long customerId) {
        List<OrderEntity> orders = orderRepo.findByCustomerId(customerId);

        return orders.stream().map(order -> {
            List<OrderItemDTO> itemDTOs = order.getItems().stream().map(item ->
                    OrderItemDTO.builder()
                            .productName(item.getProduct().getName())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .weight(item.getWeight())
                            .build()
            ).toList();

            return OrderResponseDTO.builder()
                    .id(order.getId())
                    .status(order.getStatus().name())
                    .totalAmount(order.getTotalAmount())
                    .placedAt(order.getPlacedAt())
                    .items(itemDTOs)
                    .build();
        }).toList();
    }


    public List<AdminOrderResponseDTO> getAllOrders() {
        return orderRepo.findAll().stream().map(order -> {
            List<OrderItemDTO> itemDTOs = order.getItems().stream().map(item ->
                    OrderItemDTO.builder()
                            .productName(item.getProduct().getName())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .weight(item.getWeight())
                            .build()
            ).toList();

            List<AddressDTO> addressDTOs = order.getCustomer().getAddresses().stream().map(address ->
                    AddressDTO.builder()
                            .houseNumber(address.getHouseNumber())
                            .street(address.getStreet())
                            .city(address.getCity())
                            .state(address.getState())
                            .pinCode(address.getPinCode())
                            .country(address.getCountry())
                            .build()
            ).toList();

            return AdminOrderResponseDTO.builder()
                    .orderId(order.getId())
                    .customerName(order.getCustomer().getName())
                    .customerEmail(order.getCustomer().getEmail())
                    .status(order.getStatus().name())
                    .totalAmount(order.getTotalAmount())
                    .placedAt(order.getPlacedAt())
                    .items(itemDTOs)
                    .addresses(addressDTOs) // ✅ Include address here
                    .build();
        }).toList();
    }

}