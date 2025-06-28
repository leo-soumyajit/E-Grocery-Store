package com.soumyajit.E_Grocery.Shop.Service;

import com.soumyajit.E_Grocery.Shop.DTOS.ProductDTO;
import com.soumyajit.E_Grocery.Shop.DTOS.ProductDTOforAdmin;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    public ProductDTO createProduct(ProductDTO dto, MultipartFile imageFile);
    List<ProductDTO> getAllActiveProducts();
    ProductDTO updateProduct(Long id, ProductDTO dto);
    void deActivateProduct(Long id);
    public void activateProduct(Long id);
    public List<ProductDTOforAdmin> getAllProductsForAdmin();
}
