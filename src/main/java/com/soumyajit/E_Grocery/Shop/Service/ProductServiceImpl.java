package com.soumyajit.E_Grocery.Shop.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.soumyajit.E_Grocery.Shop.DTOS.DiscountDTO;
import com.soumyajit.E_Grocery.Shop.DTOS.ProductDTO;
import com.soumyajit.E_Grocery.Shop.DTOS.ProductDTOforAdmin;
import com.soumyajit.E_Grocery.Shop.DTOS.ProductDTOforUpdate;
import com.soumyajit.E_Grocery.Shop.Entities.Product;
import com.soumyajit.E_Grocery.Shop.Entities.Roles;
import com.soumyajit.E_Grocery.Shop.Entities.User;
import com.soumyajit.E_Grocery.Shop.Exception.ResourceNotFound;
import com.soumyajit.E_Grocery.Shop.Repository.CartRepository;
import com.soumyajit.E_Grocery.Shop.Repository.ProductRepository;
import com.soumyajit.E_Grocery.Shop.Repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final CartRepository cartRepository;
    @Autowired
    private Cloudinary cloudinary;
    private final UserRepository userRepository;
    @Autowired
    private JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String senderEmail;


    @Override
    public ProductDTO createProduct(ProductDTO dto, MultipartFile imageFile) {
        try {
            Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());
            String imageUrl = uploadResult.get("secure_url").toString();

            Product product = Product.builder()
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .unitQuantity(dto.getUnitQuantity())
                    .unitLabel(dto.getUnitLabel())
                    .unitPrice(dto.getUnitPrice())
                    .imageUrl(imageUrl)
                    .active(true)
                    .build();

            return modelMapper.map(productRepository.save(product), ProductDTO.class);

        } catch (IOException e) {
            throw new RuntimeException("Image upload failed", e);
        }
    }




    @Override
    public List<ProductDTO> getAllActiveProducts() {
        LocalDateTime now = LocalDateTime.now();

        return productRepository.findAllActive()
                .stream()
                .map(product -> {
                    ProductDTO dto = modelMapper.map(product, ProductDTO.class);

                    boolean hasValidDiscount = product.getDiscountedPrice() != null &&
                            product.getDiscountExpiresAt() != null &&
                            product.getDiscountExpiresAt().isAfter(now);

                    if (hasValidDiscount) {
                        BigDecimal discountPercent = product.getUnitPrice()
                                .subtract(product.getDiscountedPrice())
                                .divide(product.getUnitPrice(), 2, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100));

                        dto.setUnitPrice(product.getDiscountedPrice());
                        dto.setDiscountPercentage(discountPercent);
                        dto.setDiscountExpiresAt(product.getDiscountExpiresAt());
                    } else {
                        dto.setUnitPrice(product.getUnitPrice());
                        dto.setDiscountPercentage(null);
                        dto.setDiscountExpiresAt(null);
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }




    @Override
    public List<ProductDTOforAdmin> getAllProductsForAdmin() {
        LocalDateTime now = LocalDateTime.now();

        return productRepository.findAll()
                .stream()
                .map(product -> {
                    ProductDTOforAdmin dto = modelMapper.map(product, ProductDTOforAdmin.class);

                    if (product.getDiscountedPrice() != null &&
                            product.getDiscountExpiresAt() != null &&
                            product.getDiscountExpiresAt().isAfter(now)) {

                        // Calculate discount percentage
                        BigDecimal discountPercent = product.getUnitPrice()
                                .subtract(product.getDiscountedPrice())
                                .divide(product.getUnitPrice(), 2, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100));

                        dto.setUnitPrice(product.getDiscountedPrice()); // override original price
                        dto.setDiscountPercentage(discountPercent);
                        dto.setDiscountExpiresAt(product.getDiscountExpiresAt());
                    } else {
                        // Clean up expired discounts (optional, only for DTO)
                        dto.setDiscountPercentage(null);
                        dto.setDiscountExpiresAt(null);
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }




    @Override
    public ProductDTO updateProduct(Long id, ProductDTOforUpdate dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Product not found with id " + id));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setUnitQuantity(BigDecimal.valueOf(dto.getUnitQuantity()));
        product.setUnitLabel(dto.getUnitLabel());
        product.setUnitPrice(BigDecimal.valueOf(dto.getUnitPrice()));
        return modelMapper.map(productRepository.save(product), ProductDTO.class);
    }

    @Override
    public String updateProductImage(Long productId, MultipartFile imageFile) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFound("Product not found with id " + productId));

        try {
            Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());
            String imageUrl = uploadResult.get("secure_url").toString();

            product.setImageUrl(imageUrl);
            productRepository.save(product);

            return imageUrl;

        } catch (IOException e) {
            throw new RuntimeException("Image upload failed", e);
        }
    }





    @Override
    @Transactional
    public void deActivateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Product not found with id " + id));

        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void activateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Product not found with id " + id));

        product.setActive(true);
        productRepository.save(product);
    }

    @Override
    public ProductDTO applyDiscount(Long productId, DiscountDTO dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFound("Product not found with id " + productId));

        BigDecimal originalPrice = product.getUnitPrice();
        BigDecimal discountAmount = originalPrice.multiply(dto.getDiscountPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal discountedPrice = originalPrice.subtract(discountAmount);

        product.setDiscountPercentage(dto.getDiscountPercentage());
        product.setDiscountedPrice(discountedPrice);
        product.setDiscountExpiresAt(LocalDateTime.now().plusHours(dto.getDurationInHours()));

        productRepository.save(product);

        log.info("✅ Discount of {}% applied on '{}'. New price: ₹{}",
                dto.getDiscountPercentage(), product.getName(), discountedPrice);

        sendDiscountEmailToUsers(product);
        return modelMapper.map(product, ProductDTO.class);
    }


    @Override
    public List<ProductDTO> searchProducts(String keyword) {
        LocalDateTime now = LocalDateTime.now();

        List<Product> products = productRepository.searchByNameContaining(keyword);

        return products.stream().map(product -> {
            ProductDTO dto = modelMapper.map(product, ProductDTO.class);

            // Only include discount if it's still active
            if (product.getDiscountedPrice() != null &&
                    product.getDiscountExpiresAt() != null &&
                    product.getDiscountExpiresAt().isAfter(now)) {
                BigDecimal discountPercent = product.getUnitPrice()
                        .subtract(product.getDiscountedPrice())
                        .divide(product.getUnitPrice(), 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                dto.setUnitPrice(product.getDiscountedPrice());
                dto.setDiscountPercentage(discountPercent);
                dto.setDiscountExpiresAt(product.getDiscountExpiresAt());
            } else {
                dto.setDiscountPercentage(null);
                dto.setDiscountExpiresAt(null);
            }

            return dto;
        }).collect(Collectors.toList());
    }


















    @Async
    private void sendDiscountEmailToUsers(Product product) {
//        List<User> users = userRepository.findByRolesIgnoreCase("USER");
        List<User> users = userRepository.findAll();

        if (users.isEmpty()) {
            log.warn("⚠️ No users with role USER found to send discount emails.");
            return;
        }

        String subject = "🔥 " + product.getName() + " is Now on Discount!";
        String body = buildDiscountEmailBody(product);

        for (User user : users) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);

                helper.setFrom(senderEmail);
                helper.setTo(user.getEmail());
                helper.setSubject(subject);
                helper.setText(body, true);

                mailSender.send(message);
                log.info("📧 Discount email sent to {}", user.getEmail());

            } catch (MessagingException e) {
                log.error("❌ Failed to send email to {}: {}", user.getEmail(), e.getMessage());
            }
        }
    }


    private String buildDiscountEmailBody(Product product) {
        BigDecimal discountPercent = product.getUnitPrice()
                .subtract(product.getDiscountedPrice())
                .divide(product.getUnitPrice(), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        String expiresAt = product.getDiscountExpiresAt()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        return "<div style='font-family: Arial, sans-serif; color: #333; padding: 20px;'>"
                + "<div style='text-align: center;'>"
                + "<img src='https://res.cloudinary.com/dek6gftbb/image/upload/v1751231533/FlashSaleImage_wj7ura.jpg' "
                + "alt='Flash Sale Banner' style='width: 100%; max-width: 600px; border-radius: 8px;'>"
                + "</div>"

                + "<h2 style='color: #e63946;'>🔥 " + product.getName() + " is Now on Discount!</h2>"
                + "<p style='font-size: 16px;'>Enjoy a limited-time offer on <strong>" + product.getName() + "</strong>.</p>"

                + "<table style='width: 100%; max-width: 600px; margin-top: 15px; border-collapse: collapse;'>"
                + "<tr><td style='padding: 8px;'><strong>Original Price:</strong></td><td>₹" + product.getUnitPrice() + "</td></tr>"
                + "<tr><td style='padding: 8px;'><strong>Discounted Price:</strong></td><td>₹" + product.getDiscountedPrice() + "</td></tr>"
                + "<tr><td style='padding: 8px;'><strong>Discount:</strong></td><td>" + discountPercent.stripTrailingZeros().toPlainString() + "% OFF</td></tr>"
                + "<tr><td style='padding: 8px;'><strong>Valid Until:</strong></td><td>" + expiresAt + "</td></tr>"
                + "</table>"

                + "<div style='margin-top: 20px;'>"
                + "<a href='https://yourwebsite.com/products/" + product.getId() + "' "
                + "style='display: inline-block; padding: 12px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px;'>"
                + "🛒 Shop Now</a>"
                + "</div>"

                + "<hr style='margin-top: 40px;'>"
                + "<p style='font-size: 12px; color: #777;'>You're receiving this email because you're a valued member of E-Grocery.</p>"
                + "<p style='font-size: 12px; color: #777;'>© 2025 E-Grocery. All rights reserved.</p>"
                + "</div>";
    }














}
