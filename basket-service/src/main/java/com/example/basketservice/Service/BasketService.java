package com.example.basketservice.Service;

import com.example.basketservice.Entity.Basket;
import com.example.basketservice.Entity.Dto.AddOrDeleteProductDto;
import com.example.basketservice.Entity.Dto.BasketDto;
import com.example.basketservice.Entity.Dto.CreateBasketDto;
import com.example.basketservice.Entity.Dto.UpdateBasketDto;
import com.example.basketservice.Repository.BasketRepository;
import com.example.commonservice.Entity.APIResponse;
import com.example.commonservice.Entity.Dto.ProductDto;
import com.example.commonservice.Util.AppUtil;
import com.example.commonservice.Util.CustomMapper;
import com.example.commonservice.Util.JwtToken;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class BasketService {
    private final Logger logger = LoggerFactory.getLogger(BasketService.class);

    private final String PRODUCT_SERVICE_URI = "http://localhost:9000/product-service/api/product/";

    @Value("${application.security.jwt.secret-key}")
    private String SECRET_KEY;

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private CustomMapper customMapper;

    @Autowired
    private JwtToken jwtToken;

    @Autowired
    private AppUtil appUtil;

    public APIResponse getBasketById(UUID id) {
        if (id == null) {
            logger.info("basket id was null");
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "basket id can not be null"
            );
        }

        logger.info("getting basket with id: {}", id);
        Basket basket = getById(id);
        if (basket == null) {
            logger.info("no basket found for id: {}", id);
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no basket found"
            );
        }

        logger.info("getBasketById was success for id: {}", id);
        BasketDto basketDto = customMapper.map(basket, BasketDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                basketDto
        );
    }

    public APIResponse getBasketByUserId(UUID userId) {
        if (userId == null) {
            logger.info("user id was null");
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "user id can not be null"
            );
        }

        logger.info("getting basket with user id: {}", userId);
        Basket basket = basketRepository.findBasketByUserId(userId);
        if (basket == null) {
            logger.info("no basket found for user id: {}", userId);
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no basket found"
            );
        }

        logger.info("getBasketByUserId success with userId: {}", userId);
        BasketDto basketDto = customMapper.map(basket, BasketDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                basketDto
        );
    }

    public APIResponse createBasket(CreateBasketDto createBasketDto) {
        if (createBasketDto.getUserId() == null) {
            logger.info("user id was null");
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "user id can not be null"
            );
        }

        logger.info("creating basket : {}", createBasketDto);
        Basket basket = basketRepository.save(customMapper.map(createBasketDto, Basket.class));
        logger.info("created basket with id: {}", basket.getId());
        BasketDto basketDto = customMapper.map(basket, BasketDto.class);
        return new APIResponse(
                HttpStatus.CREATED,
                "success",
                basketDto
        );
    }


    public APIResponse updateBasket(UpdateBasketDto updateBasketDto) {
        if (updateBasketDto.getBasketId() == null) {
            logger.info("basket id was null");
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "basket id can not be null"
            );
        }

        //get basket
        logger.info("getting basket with id: {}", updateBasketDto.getBasketId());
        Basket basket = getById(updateBasketDto.getBasketId());
        if (basket == null) {
            logger.info("no basket found with id: {}", updateBasketDto.getBasketId());
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no basket found"
            );
        }

        //if admin wants to clear the basket
        if (updateBasketDto.getProductList().isEmpty()) {
            Map<UUID, Integer> emptyProductList = new HashMap<>();
            basket.setProductList(emptyProductList);

            logger.info("cleared basket with id: {}", updateBasketDto.getBasketId());
            basketRepository.save(basket);
            return new APIResponse(
                    HttpStatus.OK,
                    "success"
            );
        }

        logger.info("updating basket with id: {}", updateBasketDto.getBasketId());
        //update basket
        basket.setProductList(updateBasketDto.getProductList());
        logger.info("updated basket with id: {}", updateBasketDto.getBasketId());
        basketRepository.save(basket);

        return new APIResponse(
                HttpStatus.NO_CONTENT,
                "success"
        );
    }


    public APIResponse addProductToBasket(AddOrDeleteProductDto addProductDto) {
        if (addProductDto.getProductId() == null) {
            logger.info("product id was null");
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "product id can not be null"
            );
        }

        if (addProductDto.getQuantity() == 0) {
            logger.info("quantity was 0 for: {}", addProductDto);
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "quantity can not be 0"
            );
        }

        logger.info("getting basket with id: {}", addProductDto.getBasketId());
        Basket basket = getById(addProductDto.getBasketId());
        if (basket == null) {
            logger.info("no basket found with id: {}", addProductDto.getBasketId());
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no basket found"
            );
        }

        //get the product from product-service

        String token = jwtToken.getToken(SECRET_KEY).getJwtToken();
        String uri = PRODUCT_SERVICE_URI + "getProductById/" + addProductDto.getProductId();

        logger.info("getting product with id: {}", addProductDto.getProductId());
        ProductDto productDto = appUtil.sendRequest(Request.Get(uri), token, ProductDto.class);
        if (productDto == null) {
            logger.info("no product found with id: {}", addProductDto.getProductId());
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no product found"
            );
        }

        if (productDto.getStock() == 0) {
            logger.info("product out of stock with id: {}", addProductDto.getProductId());
            return new APIResponse(
                    HttpStatus.OK,
                    "product not in stock"
            );
        }

        /*if the current user basket has the product that wanted to be added
        updates its quantity(map->value) and if it doesn't have the given product
        it adds that product to basket */
        logger.info("adding product with id: {}", addProductDto.getProductId());
        addProduct(basket, addProductDto);

        logger.info("addProductToBasket success");
        return new APIResponse(
                HttpStatus.OK,
                "success"
        );

    }


    public APIResponse deleteProductFromBasket(AddOrDeleteProductDto deleteProductDto) {
        UUID productID = deleteProductDto.getProductId();
        if (productID == null) {
            logger.info("product id was null");
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "product id can not be null"
            );
        }

        if (deleteProductDto.getBasketId() == null) {
            logger.info("basket id was null");
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "basket id can not be null"
            );
        }

        if (deleteProductDto.getQuantity() == 0) {
            logger.info("quantity was 0 for: {}", deleteProductDto);
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "quantity can not be 0"
            );
        }

        logger.info("getting basket with id: {}", deleteProductDto.getBasketId());
        Basket basket = getById(deleteProductDto.getBasketId());
        if (basket == null) {
            logger.info("no basket found with id: {}", deleteProductDto.getBasketId());
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no basket found"
            );
        }

        if (!basket.getProductList().containsKey(productID)) {
            logger.info("no such product with id: {} in basket ", productID);
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "there is no such product in the basket"
            );
        }

        //if given product's quantity is more than the quantity in the basket
        if (deleteProductDto.getQuantity() > basket.getProductList().get(productID)) {
            logger.info("not enough stock for product with id: {}", productID);
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "quantity can not be greater than the quantity in the basket"
            );
        }

        //get the product
        String token = jwtToken.getToken(SECRET_KEY).getJwtToken();
        String uri = PRODUCT_SERVICE_URI + "getProductById/" + productID;

        logger.info("getting product with id: {}", productID);
        ProductDto productDto = appUtil.sendRequest(Request.Get(uri), token, ProductDto.class);
        if (productDto == null) {
            logger.info("no product found with id: {}", productID);
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no product found"
            );
        }

        //delete product from basket
        logger.info("deleting product with id: {}", productID);
        deleteProduct(basket, deleteProductDto);

        logger.info("deleteProductFromBasket was success");
        return new APIResponse(
                HttpStatus.OK,
                "success"
        );
    }


    public APIResponse clearBasketByUserId(UUID userId) {
        logger.info("getting basket with user id: {}", userId);
        Basket basket = getByUserId(userId);
        if (basket == null) {
            logger.info("no basket found with user id: {}", userId);
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no basket found"
            );
        }

        basket.setProductList(new HashMap<>());

        logger.info("saving basket with id: {}", basket.getId());
        basketRepository.save(basket);

        logger.info("clearBasketByUserId was success");
        return new APIResponse(
                HttpStatus.NO_CONTENT,
                "success"
        );

    }

    public Basket getByUserId(UUID userId) {
        return basketRepository.findBasketByUserId(userId);
    }

    public Basket getById(UUID id) {
        return basketRepository.findById(id).orElse(null);
    }

    private void addProduct(Basket basket, AddOrDeleteProductDto addProductDto) {
        Map<UUID, Integer> productList = basket.getProductList();
        UUID productId = addProductDto.getProductId();

        if (productList.containsKey(productId)) {

            Integer oldQuantity = productList.get(productId);
            Integer newQuantity = oldQuantity + addProductDto.getQuantity();

            productList.replace(productId, oldQuantity, newQuantity);

        } else {
            productList.put(productId, addProductDto.getQuantity());
        }

        basket.setProductList(productList);
        basketRepository.save(basket);
    }

    private void deleteProduct(Basket basket, AddOrDeleteProductDto deleteProductDto) {
        Map<UUID, Integer> productList = basket.getProductList();
        UUID productId = deleteProductDto.getProductId();
        Integer oldQuantity = productList.get(productId);

        if (Objects.equals(oldQuantity, deleteProductDto.getQuantity())) {

            productList.remove(productId);

        } else {

            Integer newQuantity = oldQuantity - deleteProductDto.getQuantity();
            productList.replace(productId, oldQuantity, newQuantity);

        }

        basket.setProductList(productList);
        basketRepository.save(basket);
    }

}
