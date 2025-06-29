package com.soumyajit.E_Grocery.Shop.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.soumyajit.E_Grocery.Shop.DTOS.DiscountDTO;
import com.soumyajit.E_Grocery.Shop.DTOS.ProductDTO;
import com.soumyajit.E_Grocery.Shop.DTOS.ProductDTOforAdmin;
import com.soumyajit.E_Grocery.Shop.DTOS.ProductDTOforUpdate;
import com.soumyajit.E_Grocery.Shop.Entities.Product;
import com.soumyajit.E_Grocery.Shop.Exception.ResourceNotFound;
import com.soumyajit.E_Grocery.Shop.Repository.CartRepository;
import com.soumyajit.E_Grocery.Shop.Repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final CartRepository cartRepository;
    @Autowired
    private Cloudinary cloudinary;


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

                    if (product.getDiscountedPrice() != null &&
                            product.getDiscountExpiresAt() != null &&
                            product.getDiscountExpiresAt().isAfter(now)) {

                        // Calculate discount percentage: ((original - discounted) / original) * 100
                        BigDecimal discountPercent = product.getUnitPrice()
                                .subtract(product.getDiscountedPrice())
                                .divide(product.getUnitPrice(), 2, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100));

                        dto.setUnitPrice(product.getDiscountedPrice());
                        dto.setDiscountPercentage(discountPercent);
                        dto.setDiscountExpiresAt(product.getDiscountExpiresAt());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }



    @Override
    public List<ProductDTOforAdmin> getAllProductsForAdmin() {
        return productRepository.findAll()
                .stream()
                .map(product -> modelMapper.map(product, ProductDTOforAdmin.class))
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
        return modelMapper.map(product, ProductDTO.class);
    }



}
