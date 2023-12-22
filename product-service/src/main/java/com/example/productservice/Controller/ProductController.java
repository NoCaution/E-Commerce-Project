package com.example.productservice.Controller;

import com.example.commonservice.Entity.APIResponse;
import com.example.commonservice.Entity.Dto.OrderDto;
import com.example.commonservice.Entity.Dto.ProductDto;
import com.example.commonservice.Entity.Dto.UserDetailsDto;
import com.example.commonservice.Entity.Enum.Category;
import com.example.commonservice.Entity.Enum.OrderStatus;
import com.example.commonservice.Entity.Enum.Role;
import com.example.commonservice.Service.CommonService;
import com.example.commonservice.Util.AppUtil;
import com.example.commonservice.Util.CustomMapper;
import com.example.commonservice.Util.JwtToken;
import com.example.productservice.Entity.Dto.*;
import com.example.productservice.Entity.Product;
import com.example.productservice.Service.ProductService;
import jakarta.annotation.Nullable;
import org.apache.http.client.fluent.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    private final String ORDER_SERVICE_URI = "http://localhost:9000/api/order/";

    @Value("${application.security.jwt.secret-key}")
    private String SECRET_KEY;

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomMapper customMapper;

    @Autowired
    private AppUtil appUtil;

    @Autowired
    private JwtToken jwtToken;

    @Autowired
    private CommonService commonService;

    @Procedure("this is to get the product with given id")
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

    @Procedure("this is to get products with given id list. If you want to get all the products, dont give anything")
    @GetMapping("/getProducts")
    public APIResponse getProducts(@Nullable @RequestParam List<UUID> productIdList) {
        List<Product> productList;

        if (productIdList == null) {
            productList = productService.getProducts();
        } else {
            productList = productService.getProducts(productIdList);
        }

        if (productList.isEmpty()) {
            return new APIResponse(
                    HttpStatus.OK,
                    "success",
                    productList
            );
        }

        List<ProductDto> productDtoList = customMapper.convertList(productList, ProductDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                productDtoList
        );
    }

    @Procedure("this is to get the product with given category")
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
                    HttpStatus.OK,
                    "success",
                    productList
            );
        }

        List<ProductDto> productDtoList = customMapper.convertList(productList, ProductDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                productDtoList
        );
    }

    @Procedure("this is to get the product with category and subCategory which exist in CategoryDto")
    @GetMapping("/getProductsByCategoryAndSubCategory")
    public APIResponse getProductsByCategoryAndSubCategory(@RequestBody CategoryDto categoryDto) {
        if (categoryDto.getCategory() == null || categoryDto.getSubCategory() == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "category or subCategory can not be null"
            );
        }

        List<Product> productList = productService.getProductsByCategory(categoryDto);
        if (productList.isEmpty()) {
            return new APIResponse(
                    HttpStatus.OK,
                    "success",
                    productList

            );
        }

        List<ProductDto> productDtoList = customMapper.convertList(productList, ProductDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                productDtoList
        );
    }

    @Procedure("this is to update the stock of the product(s) that exist in the order with given order id")
    @PutMapping("/updateProductStockByOrderId/{orderId}")
    public APIResponse updateProductStockByOrderId(@PathVariable UUID orderId) {
        UserDetailsDto loggedInUser = commonService.getLoggedInUser();

        if (orderId == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "order id can not be null"
            );
        }

        //get the order

        String uri;
        if (loggedInUser.getRole() == Role.ADMIN) {
            uri = ORDER_SERVICE_URI + "getOrderById/" + orderId;
        } else {
            uri = ORDER_SERVICE_URI + "getLoggedInUserOrderById/" + orderId;
        }

        String token = jwtToken.getToken(SECRET_KEY).getJwtToken();
        OrderDto orderDto = appUtil.sendRequest(Request.Get(uri), token, OrderDto.class);
        if (orderDto == null) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no order found"
            );
        }

        if (orderDto.getOrderStatus() == OrderStatus.APPROVED) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "order already approved"
            );
        }

        Map<UUID, Integer> purchasedProductsMap = orderDto.getProductList();
        List<UUID> purchasedProductIdList = purchasedProductsMap.keySet().stream().toList();

        List<Product> purchasedProductList = new ArrayList<>();

        //if order is a new order
        if (orderDto.getOrderStatus() != OrderStatus.DENIED) {

            for (UUID productId : purchasedProductIdList) {
                Product product = productService.getProductById(productId);
                Integer quantity = purchasedProductsMap.get(productId);

                product.setStock(product.getStock() - quantity);
                purchasedProductList.add(product);
            }

            productService.updateProducts(purchasedProductList);
            return new APIResponse(
                    HttpStatus.OK,
                    "success"
            );
        }

        //if admin denied the order
        for (UUID productId : purchasedProductIdList) {
            Product product = productService.getProductById(productId);
            Integer quantity = purchasedProductsMap.get(productId);

            product.setStock(product.getStock() + quantity);
            purchasedProductList.add(product);
        }

        productService.updateProducts(purchasedProductList);
        return new APIResponse(
                HttpStatus.OK,
                "success"
        );
    }

    @Procedure("this is to create a product")
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

    @Procedure("this is to update a product ")
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

    @Procedure("this is to delete a product with given id")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteProductById/{id}")
    public APIResponse deleteProductById(@PathVariable UUID id) {
        if (id == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "product id can not be null"
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
