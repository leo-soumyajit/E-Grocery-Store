package com.soumyajit.E_Grocery.Shop.Service;

import com.soumyajit.E_Grocery.Shop.DTOS.DiscountDTO;
import com.soumyajit.E_Grocery.Shop.DTOS.ProductDTO;
import com.soumyajit.E_Grocery.Shop.DTOS.ProductDTOforAdmin;
import com.soumyajit.E_Grocery.Shop.DTOS.ProductDTOforUpdate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    public ProductDTO createProduct(ProductDTO dto, MultipartFile imageFile);
    List<ProductDTO> getAllActiveProducts();
    ProductDTO updateProduct(Long id, ProductDTOforUpdate dto);
    void deActivateProduct(Long id);
    public void activateProduct(Long id);
    public List<ProductDTOforAdmin> getAllProductsForAdmin();
    public String updateProductImage(Long productId, MultipartFile imageFile);
    public ProductDTO applyDiscount(Long productId, DiscountDTO dto);
}
