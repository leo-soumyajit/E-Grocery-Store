package com.soumyajit.E_Grocery.Shop.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.soumyajit.E_Grocery.Shop.DTOS.ProductDTO;
import com.soumyajit.E_Grocery.Shop.DTOS.ProductDTOforAdmin;
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
        return productRepository.findAllActive()
                .stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
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
    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Product not found with id " + id));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setUnitQuantity(dto.getUnitQuantity());
        product.setUnitLabel(dto.getUnitLabel());
        product.setUnitPrice(dto.getUnitPrice());
        product.setImageUrl(dto.getImageUrl());

        return modelMapper.map(productRepository.save(product), ProductDTO.class);
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


}
