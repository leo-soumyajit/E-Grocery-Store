package com.soumyajit.E_Grocery.Shop.Service;

import com.soumyajit.E_Grocery.Shop.DTOS.*;
import com.soumyajit.E_Grocery.Shop.EmailService.SendInvoiceEmailService;
import com.soumyajit.E_Grocery.Shop.Entities.*;
import com.soumyajit.E_Grocery.Shop.Exception.ResourceNotFound;
import com.soumyajit.E_Grocery.Shop.NotificationService.TwilioService;
import com.soumyajit.E_Grocery.Shop.Repository.CartRepository;
import com.soumyajit.E_Grocery.Shop.Repository.OrderRepository;
import com.soumyajit.E_Grocery.Shop.Repository.ProductRepository;
import com.soumyajit.E_Grocery.Shop.Repository.UserRepository;
import com.soumyajit.E_Grocery.Shop.config.OrderMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
    @Autowired
    private TwilioService twilioService;
    @Autowired
    private JavaMailSender mailSender;


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
        String customerPhone = String.valueOf(order.getCustomer().getMob_no()); // Should be in E.164 format, e.g., +91xxxxxxxxxx

        if (status == OrderStatus.ACCEPTED || status == OrderStatus.REJECTED) {
            OrderDTO orderDTO = orderMapper.toDto(order);
            sendStatusUpdateEmail(orderDTO, status.name());
        }


        switch (status) {
            case ACCEPTED -> {
                order.setAcceptedAt(LocalDateTime.now());
                twilioService.sendWhatsApp(customerPhone,
                        "✅ Your order #" + order.getId() + " has been *ACCEPTED*.\nWe will notify you once it is out for delivery.");
            }
            case DELIVERED -> {
                order.setDeliveredAt(LocalDateTime.now());
                OrderDTO orderDTO = orderMapper.toDto(order);
                sendInvoiceEmailService.sendInvoice(orderDTO);

                twilioService.sendWhatsApp(customerPhone,
                        "📦 Good news! Your order #" + order.getId() + " has been *DELIVERED*.\nThank you for shopping with us!");
            }
            case REJECTED -> {
                twilioService.sendWhatsApp(customerPhone,
                        "❌ We regret to inform you that your order #" + order.getId() + " has been *REJECTED*.\nPlease contact support for details.");
            }
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

    //send status update email
    public void sendStatusUpdateEmail(OrderDTO orderDTO, String status) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("newssocialmedia2025@gmail.com");
            helper.setTo(orderDTO.getCustomerEmail());

            String subject = "🧾 Your E-Grocery Order #" + orderDTO.getOrderId() + " has been " + status;
            helper.setSubject(subject);

            // Build item table with styling
            StringBuilder itemTable = new StringBuilder();
            itemTable.append("<table style='width:100%; border-collapse: collapse; font-family: Arial, sans-serif;'>")
                    .append("<thead style='background-color: #f5f5f5; color: #333;'>")
                    .append("<tr>")
                    .append("<th style='padding: 8px; border: 1px solid #ddd;'>Product</th>")
                    .append("<th style='padding: 8px; border: 1px solid #ddd;'>Quantity</th>")
                    .append("<th style='padding: 8px; border: 1px solid #ddd;'>Price (₹)</th>")
                    .append("<th style='padding: 8px; border: 1px solid #ddd;'>Weight</th>")
                    .append("</tr>")
                    .append("</thead><tbody>");

            for (OrderItemDTO item : orderDTO.getItems()) {
                itemTable.append("<tr>")
                        .append("<td style='padding: 8px; border: 1px solid #ddd;'>").append(item.getProductName()).append("</td>")
                        .append("<td style='padding: 8px; border: 1px solid #ddd;'>").append(item.getQuantity()).append("</td>")
                        .append("<td style='padding: 8px; border: 1px solid #ddd;'>₹").append(item.getPrice()).append("</td>")
                        .append("<td style='padding: 8px; border: 1px solid #ddd;'>").append(item.getWeight()).append("</td>")
                        .append("</tr>");
            }

            itemTable.append("</tbody></table>");

            // Color style based on status
            String statusColor = switch (status.toUpperCase()) {
                case "ACCEPTED" -> "#28a745"; // green
                case "REJECTED" -> "#dc3545"; // red
                case "DELIVERED" -> "#007bff"; // blue
                default -> "#333";            // dark gray
            };

            StringBuilder body = new StringBuilder();
            body.append("<div style='font-family: Arial, sans-serif; color: #333;'>");
            body.append("<h2 style='color: ").append(statusColor).append(";'>Your Order is ").append(status).append("</h2>");

            body.append("<p>Hi <strong>").append(orderDTO.getCustomerName()).append("</strong>,</p>");
            body.append("<p>Your order <strong>#").append(orderDTO.getOrderId())
                    .append("</strong> has been <strong style='color:").append(statusColor).append(";'>").append(status).append("</strong>.</p>");

            body.append("<p><strong>📅 Placed At:</strong> ")
                    .append(orderDTO.getPlacedAt().toLocalDate())
                    .append(" at ")
                    .append(orderDTO.getPlacedAt().toLocalTime().withNano(0))
                    .append("</p>");

            body.append("<p><strong>💰 Total Amount:</strong> ₹").append(orderDTO.getTotalAmount()).append("</p>");
            body.append("<br><h4 style='margin-bottom: 5px;'>🛒 Items Ordered:</h4>").append(itemTable);

            if ("REJECTED".equalsIgnoreCase(status)) {
                body.append("<br><p style='color:red;'><strong>❗ Reason:</strong> Unfortunately, your order was rejected. Please contact support for more details.</p>");
            }

            body.append("<br><div style='padding:10px; background-color:#f0f8ff; border-left: 4px solid #25D366;'>")
                    .append("<p>📲 Want real-time updates on <strong>WhatsApp</strong>?<br>")
                    .append("Just send <code style='color: #333;'>join exclaimed-call</code> to <strong>+1 (415) 523-8886</strong>.</p>")
                    .append("</div>");

            body.append("<p>🙏 Thanks for shopping with <strong style='color:#28a745;'>E-Grocery</strong>!</p>");
            body.append("</div>");

            helper.setText(body.toString(), true); // Send as HTML
            mailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
            // Optional: logging
        }
    }







}