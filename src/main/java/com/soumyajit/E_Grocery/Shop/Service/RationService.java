package com.soumyajit.E_Grocery.Shop.Service;

import com.soumyajit.E_Grocery.Shop.DTOS.RationItemDTO;
import com.soumyajit.E_Grocery.Shop.DTOS.RationItemUpdateRequest;
import com.soumyajit.E_Grocery.Shop.DTOS.RationListDTO;
import com.soumyajit.E_Grocery.Shop.EmailService.EmailService;
import com.soumyajit.E_Grocery.Shop.Entities.CartItem;
import com.soumyajit.E_Grocery.Shop.Entities.Product;
import com.soumyajit.E_Grocery.Shop.Entities.RationItem;
import com.soumyajit.E_Grocery.Shop.Entities.RationList;
import com.soumyajit.E_Grocery.Shop.Entities.User;
import com.soumyajit.E_Grocery.Shop.Repository.CartRepository;
import com.soumyajit.E_Grocery.Shop.Repository.ProductRepository;
import com.soumyajit.E_Grocery.Shop.Repository.RationListRepository;
import com.soumyajit.E_Grocery.Shop.Repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RationService {

    private final RationListRepository rationRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final CartRepository cartRepo;
    private final JavaMailSender mailSender;

    public void addOrUpdateRationItem(String email, RationItemDTO dto) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepo.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        RationList list = rationRepo.findByUser(user)
                .orElseGet(() -> {
                    RationList newList = RationList.builder()
                            .user(user)
                            .items(new ArrayList<>())  // ✅ initialize empty list
                            .build();
                    return rationRepo.save(newList);
                });

        // ✅ Ensure the items list is initialized (in case it was not)
        if (list.getItems() == null) {
            list.setItems(new ArrayList<>());
        }

        // Check if the product already exists in the list
        Optional<RationItem> existingItemOpt = list.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(dto.getProductId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            // Update quantity
            RationItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + dto.getQuantity());
        } else {
            // Add new item
            RationItem newItem = RationItem.builder()
                    .product(product)
                    .quantity(dto.getQuantity())
                    .build();
            list.getItems().add(newItem);
        }

        rationRepo.save(list);
    }


    public void updateRationItem(User user, RationItemUpdateRequest request) {
        RationList list = rationRepo.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ration list not found"));

        Optional<RationItem> optionalItem = list.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(request.getProductId()))
                .findFirst();

        if (optionalItem.isPresent()) {
            RationItem item = optionalItem.get();

            if (request.getQuantity() <= 0) {
                list.getItems().remove(item); // optional: remove item if quantity is 0
            } else {
                item.setQuantity(request.getQuantity());
            }

            rationRepo.save(list);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found in ration list.");
        }
    }










    public RationListDTO getMyRationList(User user) {
        RationList list = rationRepo.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ration list not found"));

        List<RationItemDTO> itemDTOs = list.getItems().stream().map(item -> {
            Product p = item.getProduct();

            // Always use original price (ignore discount)
            BigDecimal price = p.getUnitPrice();

            return new RationItemDTO(
                    item.getId(),
                    p.getId(),
                    p.getName(),
                    p.getImageUrl(),
                    price,
                    item.getQuantity(),
                    p.getUnitLabel()
            );

        }).collect(Collectors.toList());

        return new RationListDTO(list.getId(), itemDTOs);
    }




    public void deleteMyRationList(User user) {
        RationList list = rationRepo.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ration list not found"));
        rationRepo.delete(list);
    }




    public String checkoutRationList(User user) {
        RationList list = rationRepo.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ration list not found"));

        // Ensure ownership check (although redundant since it's fetched by current user)
        if (!list.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to access this list.");
        }

        list.getItems().forEach(it -> {
            CartItem ci = new CartItem();
            ci.setCustomer(user);
            ci.setProduct(it.getProduct());
            ci.setQuantity(it.getQuantity());

            // Use real-time price to avoid stale discount bugs
            BigDecimal pricePerUnit = it.getProduct().getUnitPrice();
            Integer quantity = it.getQuantity();

            if (pricePerUnit != null && quantity != null) {
                BigDecimal totalPrice = pricePerUnit.multiply(BigDecimal.valueOf(quantity));
                ci.setTotalPrice(totalPrice);
            } else {
                ci.setTotalPrice(BigDecimal.ZERO);
            }

            ci.setTotalQuantity(BigDecimal.valueOf(it.getQuantity()));
            cartRepo.save(ci);
        });

        return "✅ Products added to your cart. Please checkout the cart.";
    }





    // Scheduled for testing: runs every minute
    // Final cron (1st of every month at 8 AM): "0 0 8 1 * ?"
//    @Scheduled(cron = "0 * * * * *")
    @Scheduled(cron = "0 0 8 1 * ?")

    @Transactional
    public void sendMonthlyRationEmails() {
        List<RationList> lists = rationRepo.findAll(); // entity graph will handle the eager loading
        for (RationList list : lists) {
            if (list.getItems() != null && !list.getItems().isEmpty()) {
                sendMonthlyRationEmail(list);
            }
        }
    }

    private void sendMonthlyRationEmail(RationList list) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, true, "UTF-8");

            h.setTo(list.getUser().getEmail());
            h.setFrom("egrocerystoree@gmail.com");
            h.setSubject("🛒 Your Monthly Ration is Ready!");

            String htmlBody = buildRationEmailBody(list);
            h.setText(htmlBody, true);

            mailSender.send(msg);
        } catch (MessagingException e) {
            e.printStackTrace(); // Optionally log this properly
        }
    }

    private String buildRationEmailBody(RationList list) {
        StringBuilder sb = new StringBuilder();
        BigDecimal totalAmount = BigDecimal.ZERO;

        sb.append("<div style='font-family: Arial, sans-serif;'>")
                .append("<h2>Hi ").append(list.getUser().getName()).append(",</h2>")
                .append("<p>Your monthly ration list is ready. You can auto-order it in one click!</p>")

                // Table start
                .append("<table style='border-collapse: collapse; width: 100%; font-size: 14px;'>")
                .append("<thead><tr style='background-color: #333; color: white;'>")
                .append("<th style='padding: 10px; border: 1px solid #ddd;'>Product</th>")
                .append("<th style='padding: 10px; border: 1px solid #ddd;'>Quantity</th>")
                .append("<th style='padding: 10px; border: 1px solid #ddd;'>Price (₹)</th>")
                .append("</tr></thead><tbody>");

        for (RationItem item : list.getItems()) {
            Product product = item.getProduct();
            BigDecimal unitPrice = product.getUnitPrice();
            Integer quantity = item.getQuantity();

            BigDecimal itemTotal = (unitPrice != null && quantity != null)
                    ? unitPrice.multiply(BigDecimal.valueOf(quantity))
                    : BigDecimal.ZERO;

            totalAmount = totalAmount.add(itemTotal);

            sb.append("<tr style='background-color: #f9f9f9;'>")
                    .append("<td style='padding: 10px; border: 1px solid #ddd;'>").append(product.getName()).append("</td>")
                    .append("<td style='padding: 10px; border: 1px solid #ddd;'>").append(quantity).append("</td>")
                    .append("<td style='padding: 10px; border: 1px solid #ddd;'>₹").append(itemTotal).append("</td>")
                    .append("</tr>");
        }

        // Total row
        sb.append("<tr style='font-weight: bold; background-color: #e8e8e8;'>")
                .append("<td colspan='2' style='padding: 10px; border: 1px solid #ddd; text-align: right;'>Total:</td>")
                .append("<td style='padding: 10px; border: 1px solid #ddd;'>₹").append(totalAmount).append("</td>")
                .append("</tr>");

        sb.append("</tbody></table>")

                // CTA button
                .append("<p style='margin-top: 20px;'>")
                .append("<a href='https://yourapp.com/ration/checkout?listId=").append(list.getId()).append("' ")
                .append("style='background-color: #28a745; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px;'>")
                .append("✅ Add to Cart & Checkout")
                .append("</a></p>")

                // Note
                .append("<p style='font-size: 13px; color: #666;'>If the button doesn't work, you can manually visit ")
                .append("<a href='https://yourapp.com/cart'>https://yourapp.com/cart</a> to complete your checkout.</p>")

                // Footer
                .append("<hr style='margin-top: 30px;'>")
                .append("<p style='font-size: 12px; color: #999;'>Thanks for shopping with <strong>E‑Grocery</strong>! Stay safe and healthy.</p>")

                .append("</div>");

        return sb.toString();
    }


}
