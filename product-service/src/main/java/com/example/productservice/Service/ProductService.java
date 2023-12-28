package com.example.productservice.Service;

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
import com.example.productservice.Controller.ProductController;
import com.example.productservice.Entity.Dto.CategoryDto;
import com.example.productservice.Entity.Dto.CreateProductDto;
import com.example.productservice.Entity.Dto.UpdateProductDto;
import com.example.productservice.Entity.Product;
import com.example.productservice.Repository.ProductRepository;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProductService {
    private final String ORDER_SERVICE_URI = "http://localhost:9000/order-service/api/order/";

    @Value("${application.security.jwt.secret-key}")
    private String SECRET_KEY;

    private final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CommonService commonService;

    @Autowired
    private JwtToken jwtToken;

    @Autowired
    private AppUtil appUtil;

    @Autowired
    private CustomMapper customMapper;


    public APIResponse getProductById(UUID id) {
        if (id == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "given id is not legit"
            );
        }

        logger.info("getting product by id: {}", id);
        Product product = getById(id);
        if (product == null) {
            logger.info("no product found for id: {}", id);
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no product found"
            );
        }

        ProductDto productDto = customMapper.map(product, ProductDto.class);
        logger.info("getProductById success");
        return new APIResponse(
                HttpStatus.OK,
                "success",
                productDto
        );
    }


    public APIResponse getProducts(List<UUID> productIdList) {
        logger.info("getting products");
        List<Product> productList;

        if (productIdList == null) {
            productList = productRepository.findAll();
        } else {
            productList = productRepository.findAllById(productIdList);
        }

        if (productList.isEmpty()) {
            logger.info("no product found");
            return new APIResponse(
                    HttpStatus.OK,
                    "success",
                    productList
            );
        }

        List<ProductDto> productDtoList = customMapper.convertList(productList, ProductDto.class);
        logger.info("getProducts success");
        return new APIResponse(
                HttpStatus.OK,
                "success",
                productDtoList
        );
    }


    public APIResponse getProductsByCategory(Category category) {
        if (category == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "given category is not legit"
            );
        }

        logger.info("getting products with category: {}", category);
        List<Product> productList = productRepository.findProductsByCategory(category);
        if (productList.isEmpty()) {
            logger.info("no product found with category: {}", category);
            return new APIResponse(
                    HttpStatus.OK,
                    "success",
                    productList
            );
        }

        List<ProductDto> productDtoList = customMapper.convertList(productList, ProductDto.class);
        logger.info("getProductsByCategory success");
        return new APIResponse(
                HttpStatus.OK,
                "success",
                productDtoList
        );
    }


    public APIResponse getProductsByCategory(CategoryDto categoryDto) {
        if (categoryDto.getCategory() == null || categoryDto.getSubCategory() == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "category or subCategory can not be null"
            );
        }

        logger.info("getting products with category and subCategory: {}", categoryDto);
        List<Product> productList = productRepository.findProductsByCategoryAndSubCategory(categoryDto.getCategory(), categoryDto.getSubCategory());
        if (productList.isEmpty()) {
            logger.info("no product found with: {}", categoryDto);
            return new APIResponse(
                    HttpStatus.OK,
                    "success",
                    productList

            );
        }

        List<ProductDto> productDtoList = customMapper.convertList(productList, ProductDto.class);
        logger.info("getProductsByCategory success");
        return new APIResponse(
                HttpStatus.OK,
                "success",
                productDtoList
        );
    }


    public APIResponse createProduct(CreateProductDto createProductDto) {
        if (createProductDto == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "given product is not legit"
            );
        }

        logger.info("creating product: {}", createProductDto);
        Product product = customMapper.map(createProductDto, Product.class);
        try {

            product.setImage(createProductDto.getImage().getBytes());

        } catch (Exception e) {

            logger.info("error while setting image: {}", e.getMessage(), e);
            return new APIResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "failed"
            );
        }

        productRepository.save(product);

        logger.info("creating product success: {}", product);
        ProductDto productDto = customMapper.map(product, ProductDto.class);
        return new APIResponse(
                HttpStatus.CREATED,
                "success",
                productDto
        );
    }


    public APIResponse updateProduct(UpdateProductDto updateProductDto) {
        if (updateProductDto.getId() == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "product id can not be null"
            );
        }

        logger.info("getting product: {}", updateProductDto.getId());
        Product product = getById(updateProductDto.getId());
        if (product == null) {
            logger.info("no product found for id: {}", updateProductDto.getId());
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no product found"
            );
        }

        try {

            Product updateProduct = updateProductFieldsWithNullSafety(product, updateProductDto);
            productRepository.save(updateProduct);
            return new APIResponse(
                    HttpStatus.OK,
                    "success"
            );

        } catch (IOException e) {
            logger.error("error while updating image {}", e.getMessage(), e);
            return new APIResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "failed"
            );
        }
    }


    public APIResponse updateProductStockByOrderId(UUID orderId) {
        UserDetailsDto loggedInUser = commonService.getLoggedInUser();

        if (orderId == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "order id can not be null"
            );
        }

        //get the order

        logger.info("getting order with id: {}", orderId);
        String uri;
        if (loggedInUser.getRole() == Role.ADMIN) {
            uri = ORDER_SERVICE_URI + "getOrderById/" + orderId;
        } else {
            uri = ORDER_SERVICE_URI + "getLoggedInUserOrderById/" + orderId;
        }

        String token = jwtToken.getToken(SECRET_KEY).getJwtToken();
        OrderDto orderDto = appUtil.sendRequest(Request.Get(uri), token, OrderDto.class);
        if (orderDto == null) {
            logger.info("no order found for id: {}", orderId);
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no order found"
            );
        }
        logger.info("get order success");

        if (orderDto.getOrderStatus() == OrderStatus.APPROVED) {
            logger.info("bad request for order: {}", orderDto);
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "order already approved"
            );
        }


        logger.info("updating products");
        Map<UUID, Integer> purchasedProductsMap = orderDto.getProductList();
        List<UUID> purchasedProductIdList = purchasedProductsMap.keySet().stream().toList();

        List<Product> purchasedProductList = new ArrayList<>();

        //if order is a new order
        if (orderDto.getOrderStatus() != OrderStatus.DENIED) {

            for (UUID productId : purchasedProductIdList) {
                Product product = getById(productId);
                Integer quantity = purchasedProductsMap.get(productId);

                product.setStock(product.getStock() - quantity);
                purchasedProductList.add(product);
            }

            productRepository.saveAll(purchasedProductList);
            logger.info("update products success with orderId: {}", orderId);
            return new APIResponse(
                    HttpStatus.OK,
                    "success"
            );
        }

        //if admin denied the order
        for (UUID productId : purchasedProductIdList) {
            Product product = getById(productId);
            Integer quantity = purchasedProductsMap.get(productId);

            product.setStock(product.getStock() + quantity);
            purchasedProductList.add(product);
        }


        productRepository.saveAll(purchasedProductList);
        logger.info("update products success with orderId: {}", orderId);
        return new APIResponse(
                HttpStatus.OK,
                "success"
        );
    }


    public APIResponse deleteProduct(UUID id) {
        if (id == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "product id can not be null"
            );
        }

        logger.info("getting product with id: {}", id);
        Product product = getById(id);
        if (product == null) {
            logger.info("no product found with id: {}", id);
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no product found"
            );
        }

        productRepository.deleteById(id);
        logger.info("delete product success");
        return new APIResponse(
                HttpStatus.NO_CONTENT,
                "success"
        );
    }


    private Product updateProductFieldsWithNullSafety(Product product, UpdateProductDto updateProductDto) throws IOException {
        product.setProductName(updateProductDto.getProductName() == null ? product.getProductName() : updateProductDto.getProductName());
        product.setPrice(updateProductDto.getPrice() == null ? product.getPrice() : updateProductDto.getPrice());
        product.setImage(updateProductDto.getImage() == null ? product.getImage() : updateProductDto.getImage().getBytes());
        product.setStock(updateProductDto.getStock() == null ? product.getStock() : updateProductDto.getStock());
        product.setCategory(updateProductDto.getCategory() == null ? product.getCategory() : updateProductDto.getCategory());
        product.setSubCategory(updateProductDto.getSubCategory() == null ? product.getSubCategory() : updateProductDto.getSubCategory());
        return product;
    }

    private Product getById(UUID id) {
        return productRepository.findById(id).orElse(null);
    }
}
