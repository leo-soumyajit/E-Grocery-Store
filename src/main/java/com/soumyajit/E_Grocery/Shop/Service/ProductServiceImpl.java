package com.soumyajit.E_Grocery.Shop.Service;

import com.soumyajit.E_Grocery.Shop.DTOS.ProductDTO;
import com.soumyajit.E_Grocery.Shop.Entities.Product;
import com.soumyajit.E_Grocery.Shop.Exception.ResourceNotFound;
import com.soumyajit.E_Grocery.Shop.Repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    public ProductDTO createProduct(ProductDTO dto) {
        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .unitQuantity(dto.getUnitQuantity())
                .unitLabel(dto.getUnitLabel())
                .unitPrice(dto.getUnitPrice())
                .imageUrl(dto.getImageUrl())
                .build();

        return modelMapper.map(productRepository.save(product), ProductDTO.class);
    }


    @Override
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
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
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Product not found with id " + id));
        productRepository.delete(product);
    }
}
