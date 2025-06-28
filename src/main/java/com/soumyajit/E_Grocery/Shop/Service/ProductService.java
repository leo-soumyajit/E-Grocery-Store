package com.soumyajit.E_Grocery.Shop.Service;

import com.soumyajit.E_Grocery.Shop.DTOS.ProductDTO;

import java.util.List;

public interface ProductService {
    ProductDTO createProduct(ProductDTO dto);
    List<ProductDTO> getAllProducts();
    ProductDTO updateProduct(Long id, ProductDTO dto);
    void deleteProduct(Long id);
}
