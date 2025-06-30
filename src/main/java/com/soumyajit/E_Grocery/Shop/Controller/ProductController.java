package com.soumyajit.E_Grocery.Shop.Controller;

import com.soumyajit.E_Grocery.Shop.Advices.ApiResponse;
import com.soumyajit.E_Grocery.Shop.DTOS.DiscountDTO;
import com.soumyajit.E_Grocery.Shop.DTOS.ProductDTO;
import com.soumyajit.E_Grocery.Shop.DTOS.ProductDTOforAdmin;
import com.soumyajit.E_Grocery.Shop.DTOS.ProductDTOforUpdate;
import com.soumyajit.E_Grocery.Shop.Service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDTO> createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("unitQuantity") double unitQuantity,
            @RequestParam("unitLabel") String unitLabel,
            @RequestParam("unitPrice") double unitPrice,
            @RequestParam("imageFile") MultipartFile imageFile
    ) {
        ProductDTO dto = ProductDTO.builder()
                .name(name)
                .description(description)
                .unitQuantity(BigDecimal.valueOf(unitQuantity))
                .unitLabel(unitLabel)
                .unitPrice(BigDecimal.valueOf(unitPrice))
                .build();

        ProductDTO created = productService.createProduct(dto, imageFile);
        return ResponseEntity.ok(created);
    }


    @GetMapping
    public List<ProductDTO> getAllActiveProducts() {
        return productService.getAllActiveProducts();
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<ProductDTOforAdmin>> getAllProductsForAdmin() {
        List<ProductDTOforAdmin> products = productService.getAllProductsForAdmin();
        return ResponseEntity.ok(products);
    }


    @PutMapping("/{id}")
    public ProductDTO updateProduct(@PathVariable Long id, @RequestBody ProductDTOforUpdate dto) {
        return productService.updateProduct(id, dto);
    }


    @PutMapping("/admin/imageUpdate/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<String>> updateProductImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile imageFile) {

        String imageUrl = productService.updateProductImage(id, imageFile);

        ApiResponse response = new ApiResponse(
                "✅ Product image updated successfully"
        );

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/deactive/{id}")
    public ResponseEntity<?> deActivateProduct(@PathVariable Long id) {
        productService.deActivateProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/active/{id}")
    public ResponseEntity<?> activateProduct(@PathVariable Long id) {
        productService.activateProduct(id);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/admin/{productId}/discount")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> applyDiscountToProduct(
            @PathVariable Long productId,
            @RequestBody DiscountDTO discountDTO) {

        ProductDTO updatedProduct = productService.applyDiscount(productId, discountDTO);

        ApiResponse response = new ApiResponse(
                "✅ Discount applied successfully to product ID: " + productId
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam("keyword") String keyword) {
        List<ProductDTO> result = productService.searchProducts(keyword);
        return ResponseEntity.ok(result);
    }

}
