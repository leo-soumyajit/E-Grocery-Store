package com.soumyajit.E_Grocery.Shop.Service;

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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RationService {

    private final RationListRepository rationRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final CartRepository cartRepo;
    private final JavaMailSender mailSender;

    public void saveRationList(String email, RationListDTO dto) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<RationItem> items = dto.getItems().stream()
                .map(it -> RationItem.builder()
                        .product(productRepo.findById(it.getProductId()).orElseThrow())
                        .quantity(it.getQuantity())
                        .build())
                .toList();

        RationList list = rationRepo.findByUser(user)
                .orElse(RationList.builder().user(user).build());

        list.setItems(items);
        rationRepo.save(list);
    }

    public String checkoutRationList(Long listId) {
        RationList list = rationRepo.findById(listId).orElseThrow();
        User user = list.getUser();

        list.getItems().forEach(it -> {
            CartItem ci = new CartItem();
            ci.setCustomer(user);
            ci.setProduct(it.getProduct());
            ci.setQuantity(it.getQuantity());

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

        return "Products added to your cart. Checkout the cart.";
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
            h.setFrom("newssocialmedia2025@gmail.com");
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
            sb.append("<tr style='background-color: #f9f9f9;'>")
                    .append("<td style='padding: 10px; border: 1px solid #ddd;'>").append(product.getName()).append("</td>")
                    .append("<td style='padding: 10px; border: 1px solid #ddd;'>").append(item.getQuantity()).append("</td>")
                    .append("<td style='padding: 10px; border: 1px solid #ddd;'>₹")
                    .append(product.getUnitPrice() != null ? product.getUnitPrice().toString() : "N/A").append("</td>")
                    .append("</tr>");
        }

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
