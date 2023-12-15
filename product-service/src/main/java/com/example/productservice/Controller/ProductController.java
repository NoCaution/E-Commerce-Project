package com.example.productservice.Controller;

import com.example.commonservice.Entity.APIResponse;
import com.example.commonservice.Util.CustomMapper;
import com.example.productservice.Entity.Dto.*;
import com.example.productservice.Entity.Enum.Category;
import com.example.productservice.Entity.Product;
import com.example.productservice.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomMapper customMapper;

    @GetMapping("/getProductById/{id}")
    public APIResponse getProductById(@PathVariable UUID id) {
        if (id == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "given id is not legit"
            );
        }

        Product product = productService.getProductById(id);
        if (product == null) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no product found"
            );
        }

        ProductDto productDto = customMapper.map(product, ProductDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                productDto
        );
    }

    @GetMapping("/getProducts")
    public APIResponse getProducts() {
        List<Product> productList = productService.getProducts();
        if (productList.isEmpty()) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no product found"
            );
        }

        List<ProductDto> productDtoList = customMapper.convertList(productList, ProductDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                productDtoList
        );
    }

    @GetMapping("/getProductsByCategory")
    public APIResponse getProductsByCategory(@RequestParam Category category) {
        if (category == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "given category is not legit"
            );
        }

        List<Product> productList = productService.getProductsByCategory(category);
        if (productList.isEmpty()) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no product found"
            );
        }

        List<ProductDto> productDtoList = customMapper.convertList(productList, ProductDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                productDtoList
        );
    }

    @GetMapping("/getProductsByCategoryAndSubCategory")
    public APIResponse getProductsByCategoryAndSubCategory(@RequestBody CategoryDto categoryDto) {
        if (categoryDto.getCategory() == null || categoryDto.getSubCategory() == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "given category or subCategory is not legit"
            );
        }

        List<Product> productList = productService.getProductsByCategory(categoryDto);
        if (productList.isEmpty()) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no product found"

            );
        }

        List<ProductDto> productDtoList = customMapper.convertList(productList, ProductDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                productDtoList
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/createProduct")
    public APIResponse createProduct(@RequestBody CreateProductDto createProductDto) {
        if (createProductDto == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "given product is not legit"
            );
        }

        Product product = customMapper.map(createProductDto, Product.class);
        productService.createOrUpdateProduct(product);

        ProductDto productDto = customMapper.map(product, ProductDto.class);
        return new APIResponse(
                HttpStatus.CREATED,
                "success",
                productDto
        );

    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updateProduct")
    public APIResponse updateProduct(@RequestBody UpdateProductDto updateProductDto) {
        if (updateProductDto == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "given product is not legit"
            );
        }

        Product product = productService.getProductById(updateProductDto.getId());
        if (product == null) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no product found"
            );
        }

        try {

            Product updateProduct = productService.updateProductFieldsWithNullSafety(product, updateProductDto);
            productService.createOrUpdateProduct(updateProduct);
            return new APIResponse(
                    HttpStatus.OK,
                    "success"
            );

        } catch (IOException e) {
            return new APIResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "updating image failed"
            );
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteProductById/{id}")
    public APIResponse deleteProductById(@PathVariable UUID id) {
        if (id == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "given id is not legit"
            );
        }

        Product product = productService.getProductById(id);
        if (product == null) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no product found"
            );
        }

        productService.deleteProduct(id);
        return new APIResponse(
                HttpStatus.NO_CONTENT,
                "success"
        );
    }
}
