package com.example.productservice.Controller;

import com.example.commonservice.Entity.APIResponse;
import com.example.commonservice.Entity.Enum.Category;
import com.example.productservice.Entity.Dto.*;
import com.example.productservice.Service.ProductService;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    @Autowired
    private ProductService productService;


    @GetMapping("/getProductById/{id}")
    public APIResponse getProductById(@PathVariable UUID id) {
        return productService.getProductById(id);
    }

    @GetMapping("/getProducts")
    public APIResponse getProducts(@Nullable @RequestParam("id") List<UUID> productIdList) {
        //if productIdList is null, all the products will be returned
        return productService.getProducts(productIdList);
    }

    @GetMapping("/getProductsByCategory")
    public APIResponse getProductsByCategory(@RequestParam Category category) {
        return productService.getProductsByCategory(category);
    }

    @GetMapping("/getProductsByCategoryAndSubCategory")
    public APIResponse getProductsByCategoryAndSubCategory(@RequestBody CategoryDto categoryDto) {
        return productService.getProductsByCategory(categoryDto);
    }

    @PutMapping("/updateProductStockByOrderId/{orderId}")
    public APIResponse updateProductStockByOrderId(@PathVariable UUID orderId) {
        return productService.updateProductStockByOrderId(orderId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/createProduct")
    public APIResponse createProduct(@RequestBody CreateProductDto createProductDto) {
        return productService.createProduct(createProductDto);

    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updateProduct")
    public APIResponse updateProduct(@RequestBody UpdateProductDto updateProductDto) {
        return productService.updateProduct(updateProductDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteProductById/{id}")
    public APIResponse deleteProductById(@PathVariable UUID id) {

        return productService.deleteProduct(id);
    }
}
