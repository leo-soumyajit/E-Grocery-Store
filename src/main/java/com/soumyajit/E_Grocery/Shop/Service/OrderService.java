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
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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


    @Transactional
    public void placeOrder(Long customerId) {
        List<CartItem> cartItems = cartRepo.findByCustomerId(customerId);
        if (cartItems.isEmpty()) {
            throw new ResourceNotFound("Cart is empty");
        }

        User customer = userRepo.findById(customerId)
                .orElseThrow(() -> new ResourceNotFound("User not found"));

        // ✅ Extract active address
        Address activeAddress = customer.getAddresses().stream()
                .filter(Address::getIsActive)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active address found"));

        // ✅ Convert to embeddable address
        EmbeddedAddress deliveryAddress = EmbeddedAddress.builder()
                .houseNumber(activeAddress.getHouseNumber())
                .street(activeAddress.getStreet())
                .city(activeAddress.getCity())
                .district(activeAddress.getDistrict())
                .state(activeAddress.getState())
                .pinCode(activeAddress.getPinCode())
                .country(activeAddress.getCountry())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cart : cartItems) {
            Product product = cart.getProduct();
            int quantity = cart.getQuantity();

            // ✅ Get discounted price if available
            BigDecimal effectivePrice = getEffectivePrice(product);

            BigDecimal itemTotalPrice = effectivePrice.multiply(BigDecimal.valueOf(quantity));
            totalAmount = totalAmount.add(itemTotalPrice);

            BigDecimal totalUnitQty = product.getUnitQuantity().multiply(BigDecimal.valueOf(quantity));
            String weight = totalUnitQty + product.getUnitLabel();

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(quantity)
                    .price(itemTotalPrice)  // total price for this item
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
                .deliveryAddress(deliveryAddress)
                .build();

        orderItems.forEach(item -> item.setOrder(order));

        orderRepo.save(order);
        cartRepo.deleteByCustomerId(customerId);

        // ✅ Prepare email DTOs
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(item -> OrderItemDTO.builder()
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .weight(item.getWeight())
                        .build()
                ).collect(Collectors.toList());

        // ✅ Send order placed email
        sendOrderPlacedEmail(
                customer.getEmail(),
                customer.getName(),
                order.getId(),
                itemDTOs
        );
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
//                twilioService.sendWhatsApp(customerPhone,
//                        "✅ Your order #" + order.getId() + " has been *ACCEPTED*.\nWe will notify you once it is out for delivery.");
            }
            case DELIVERED -> {
                order.setDeliveredAt(LocalDateTime.now());
                OrderDTO orderDTO = orderMapper.toDto(order);
                sendInvoiceEmailService.sendInvoice(orderDTO);

//                twilioService.sendWhatsApp(customerPhone,
//                        "📦 Good news! Your order #" + order.getId() + " has been *DELIVERED*.\nThank you for shopping with us!");
            }
            case REJECTED -> {
//                twilioService.sendWhatsApp(customerPhone,
//                        "❌ We regret to inform you that your order #" + order.getId() + " has been *REJECTED*.\nPlease contact support for details.");
            }
        }

        orderRepo.save(order);
    }



    public List<OrderResponseDTO> getOrdersByCustomer(Long customerId) {
        List<OrderEntity> orders = orderRepo.findByCustomerIdOrderByPlacedAtDesc(customerId);

        return orders.stream().map(order -> {
            List<OrderItemDTO> itemDTOs = order.getItems().stream().map(item ->
                    OrderItemDTO.builder()
                            .productName(item.getProduct().getName())
                            .imageUrl(item.getProduct().getImageUrl())
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
        return orderRepo.findAll().stream()
                .sorted((o1, o2) -> o2.getPlacedAt().compareTo(o1.getPlacedAt()))
                .map(order -> {
                    // Map order items to DTO
                    List<OrderItemDTO> itemDTOs = order.getItems().stream().map(item ->
                            OrderItemDTO.builder()
                                    .productName(item.getProduct().getName())
                                    .quantity(item.getQuantity())
                                    .price(item.getPrice())
                                    .weight(item.getWeight())
                                    .build()
                    ).toList();

                    // Map embedded delivery address to AddressDTO (handle null safely)
                    AddressDTO addressDTO = null;
                    if (order.getDeliveryAddress() != null) {
                        EmbeddedAddress embedded = order.getDeliveryAddress();
                        addressDTO = AddressDTO.builder()
                                .houseNumber(embedded.getHouseNumber())
                                .street(embedded.getStreet())
                                .city(embedded.getCity())
                                .district(embedded.getDistrict())
                                .state(embedded.getState())
                                .pinCode(embedded.getPinCode())
                                .country(embedded.getCountry())
                                .build();
                    }

                    // Build and return response DTO
                    return AdminOrderResponseDTO.builder()
                            .orderId(order.getId())
                            .customerName(order.getCustomer().getName())
                            .customerEmail(order.getCustomer().getEmail())
                            .mob_no(order.getCustomer().getMob_no())
                            .status(order.getStatus().name())
                            .totalAmount(order.getTotalAmount())
                            .placedAt(order.getPlacedAt())
                            .items(itemDTOs)
                            .addresses(addressDTO)
                            .build();
                })
                .toList();
    }




    public List<AdminOrderResponseDTO> getActiveOrders() {
        return orderRepo.findAllByStatusInOrderByPlacedAtDesc(List.of(OrderStatus.PENDING, OrderStatus.ACCEPTED))
                .stream().map(order -> {
                    List<OrderItemDTO> itemDTOs = order.getItems().stream().map(item ->
                            OrderItemDTO.builder()
                                    .productName(item.getProduct().getName())
                                    .quantity(item.getQuantity())
                                    .price(item.getPrice())
                                    .weight(item.getWeight())
                                    .build()
                    ).toList();

                    AddressDTO addressDTO = null;
                    if (order.getDeliveryAddress() != null) {
                        EmbeddedAddress embedded = order.getDeliveryAddress();
                        addressDTO = AddressDTO.builder()
                                .houseNumber(embedded.getHouseNumber())
                                .street(embedded.getStreet())
                                .city(embedded.getCity())
                                .district(embedded.getDistrict())
                                .state(embedded.getState())
                                .pinCode(embedded.getPinCode())
                                .country(embedded.getCountry())
                                .build();
                    }

                    return AdminOrderResponseDTO.builder()
                            .orderId(order.getId())
                            .customerName(order.getCustomer().getName())
                            .customerEmail(order.getCustomer().getEmail())
                            .mob_no(order.getCustomer().getMob_no())
                            .status(order.getStatus().name())
                            .totalAmount(order.getTotalAmount())
                            .placedAt(order.getPlacedAt())
                            .items(itemDTOs)
                            .addresses(addressDTO)
                            .build();
                }).toList();
    }




    @Transactional
    public void cancelOrder(Long orderId, String email, String reason) throws AccessDeniedException {
        OrderEntity order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFound("Order not found"));

        if (!order.getCustomer().getEmail().equals(email)) {
            throw new AccessDeniedException("You cannot cancel someone else's order");
        }

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Order cannot be cancelled");
        }

        if (order.getPlacedAt().plusMinutes(15).isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Order cancellation window has expired");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());

        if (reason != null && !reason.trim().isEmpty()) {
            order.setCancellationReason(reason.trim());
        }

        orderRepo.save(order);
        sendCancellationEmail(
                order.getCustomer().getEmail(),
                order.getCustomer().getName(),
                order.getId(),
                order.getTotalAmount(),
                LocalDateTime.now(),
                reason
        );
    }

    private BigDecimal getEffectivePrice(Product product) {
        if (product.getDiscountedPrice() != null &&
                product.getDiscountExpiresAt() != null &&
                product.getDiscountExpiresAt().isAfter(LocalDateTime.now())) {
            return product.getDiscountedPrice();
        }
        return product.getUnitPrice();
    }




    private void sendCancellationEmail(String toEmail, String customerName, Long orderId, BigDecimal totalAmount, LocalDateTime cancelledAt, String reason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("❌ Your Order #" + orderId + " has been Cancelled");

            String formattedDate = cancelledAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

            String html = """
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;">
                <div style="max-width: 600px; margin: auto; background: white; border-radius: 8px; padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.05);">
                    <div style="text-align: center;">
                        <img src="https://res.cloudinary.com/dek6gftbb/image/upload/v1751108759/grocery-store-logo-template-in-flat-design-style-vector-removebg-preview_hcbtaz.png" width="80" alt="E-Grocery Logo">
                        <h2 style="color: #ff4c4c;">❌ Order Cancelled</h2>
                    </div>

                    <p>Hi <strong>%s</strong>,</p>
                    <p>Your order <strong>#%s</strong> has been cancelled successfully.</p>

                    <div style="background-color: #fcebea; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <p><strong>🕒 Cancelled At:</strong> %s</p>
                        <p><strong>💰 Order Total:</strong> ₹%s</p>
                        <p><strong>📋 Reason:</strong> %s</p>
                    </div>

                    <p>If this was a mistake or you have questions, contact our support team.</p>

                    <p style="text-align: center; font-size: 12px; color: #aaa;">Thank you for using <strong>E-Grocery Store</strong>.</p>
                </div>
            </body>
            </html>
            """.formatted(customerName, orderId, formattedDate, totalAmount, reason);

            helper.setText(html, true);
            helper.setFrom("egrocerystoree@gmail.com");

            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace(); // Log or handle error
        }
    }




    private void sendOrderPlacedEmail(String to, String name, Long orderId, List<OrderItemDTO> items) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("egrocerystoree@gmail.com");
            helper.setTo(to);
            helper.setSubject("🛒 Order Placed Successfully — Order #" + orderId);

            StringBuilder tableBuilder = new StringBuilder();

            tableBuilder.append("""
            <table border='1' cellpadding='8' cellspacing='0' style='border-collapse: collapse; font-family: Arial, sans-serif; width: 100%;'>
                <thead style='background-color: #f2f2f2;'>
                    <tr>
                        <th>Product</th>
                        <th>Quantity</th>
                        <th>Price (₹)</th>
                        <th>Weight</th>
                    </tr>
                </thead>
                <tbody>
        """);

            for (OrderItemDTO item : items) {
                tableBuilder.append(String.format("""
                <tr>
                    <td>%s</td>
                    <td>%d</td>
                    <td>₹%.2f</td>
                    <td>%s</td>
                </tr>
            """, item.getProductName(), item.getQuantity(), item.getPrice(), item.getWeight()));
            }

            tableBuilder.append("</tbody></table>");

            String body = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2 style="color: green;">Hi %s,</h2>
                <p>Your order <strong>#%d</strong> has been <strong>successfully placed</strong> ✅.</p>
                <p>🕒 Please wait while the shop owner reviews and accepts your order.</p>
                <p>❗ You may cancel this order within <strong>15 minutes</strong> from the time it was placed.</p>
                <h3>🛒 Items Ordered:</h3>
                %s
                <br/>
                <p>Thanks for shopping with <strong>E-Grocery Store</strong>!</p>
            </body>
            </html>
        """, name, orderId, tableBuilder.toString());

            helper.setText(body, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
            // log or handle error
        }
    }





    //send status update email
    private void sendStatusUpdateEmail(OrderDTO orderDTO, String status) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("egrocerystoree@gmail.com");
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