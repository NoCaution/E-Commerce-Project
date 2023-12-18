package com.example.basketservice.Controller;

import com.example.basketservice.Entity.Basket;
import com.example.basketservice.Entity.Dto.AddOrDeleteProductDto;
import com.example.basketservice.Entity.Dto.BasketDto;
import com.example.basketservice.Entity.Dto.CreateBasketDto;
import com.example.basketservice.Entity.Dto.UpdateBasketDto;
import com.example.basketservice.Service.BasketService;
import com.example.commonservice.Entity.APIResponse;
import com.example.commonservice.Entity.Dto.ProductDto;
import com.example.commonservice.Service.CommonService;
import com.example.commonservice.Util.AppUtil;
import com.example.commonservice.Util.CustomMapper;
import com.example.commonservice.Util.JwtToken;
import org.apache.http.client.fluent.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/basket")
public class BasketController {
    private final String PRODUCT_SERVICE_URI = "http://localhost:9000/api/product/";

    @Value("${application.security.jwt.secret-key}")
    private String SECRET_KEY;

    @Autowired
    private BasketService basketService;

    @Autowired
    private CustomMapper customMapper;

    @Autowired
    private CommonService commonService;

    @Autowired
    private JwtToken jwtToken;

    @Autowired
    private AppUtil appUtil;

    @Procedure("this is to get the basket of authenticated user")
    @GetMapping("/getLoggedInUserBasket")
    public APIResponse getLoggedInUserBasket() {
        UUID loggedInUserId = UUID.fromString(commonService.getLoggedInUserId());

        Basket basket = basketService.getBasketByUserId(loggedInUserId);
        if (basket == null) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no basket found"
            );
        }

        BasketDto basketDto = customMapper.map(basket, BasketDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                basketDto
        );
    }

    @Procedure("this is to create a basket for authenticated user for the first time")
    @PostMapping("/initializeLoggedInUserBasket")
    public APIResponse initializeLoggedInUserBasket() {
        String loggedInUserId = commonService.getLoggedInUserId();
        Map<UUID, Integer> emptyItemList = new HashMap<>();

        Basket basket = new Basket(
                UUID.fromString(loggedInUserId),
                emptyItemList
        );
        basketService.createOrUpdateBasket(basket);

        return new APIResponse(
                HttpStatus.CREATED,
                "success"
        );
    }

    @Procedure("this is to empty the product list in the basket of authenticated user")
    @PutMapping("/clearLoggedInUserBasket")
    public APIResponse clearLoggedInUserBasket() {
        String loggedInUserId = commonService.getLoggedInUserId();

        Basket basket = basketService.getBasketByUserId(UUID.fromString(loggedInUserId));
        if (basket == null) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no basket found"
            );
        }

        basketService.clearBasket(basket);
        return new APIResponse(
                HttpStatus.NO_CONTENT,
                "success"
        );
    }

    @Procedure("this is to add a product to basket of authenticated user")
    @PutMapping("/addProductToLoggedInUserBasket")
    public APIResponse addProductToLoggedInUserBasket(@RequestBody AddOrDeleteProductDto addProductDto) {
        String loggedInUserId = commonService.getLoggedInUserId();
        UUID productId = addProductDto.getProductId();

        if (productId == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "product id can not be null"
            );
        }

        if (addProductDto.getQuantity() == 0) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "quantity can not be 0"
            );
        }

        Basket basket = basketService.getBasketByUserId(UUID.fromString(loggedInUserId));
        if (basket == null) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no basket found"
            );
        }

        String token = jwtToken.getToken(SECRET_KEY).getJwtToken();
        String uri = PRODUCT_SERVICE_URI + "getProductById/" + addProductDto.getProductId();

        ProductDto productDto = appUtil.sendRequest(Request.Get(uri), token, ProductDto.class);
        if (productDto == null) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no product found"
            );
        }

        if (productDto.getStock() == 0) {
            return new APIResponse(
                    HttpStatus.OK,
                    "product is not in stock"
            );
        }
        /*if the current user basket has the product that wanted to be added
          updates its quantity(map->value) and if it doesn't have the given product
          it adds that product to basket */
        basketService.addProductToBasket(basket, addProductDto);

        return new APIResponse(
                HttpStatus.OK,
                "success"
        );
    }

    @PutMapping("/deleteProductFromLoggedInUserBasket")
    public APIResponse deleteProductFromLoggedInUserBasket(@RequestBody AddOrDeleteProductDto deleteProductDto) {
        UUID loggedInUserId = UUID.fromString(commonService.getLoggedInUserId());
        UUID productId = deleteProductDto.getProductId();

        if (deleteProductDto.getProductId() == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "product id can not be null"
            );
        }

        if (deleteProductDto.getQuantity() == 0) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "quantity can not be 0"
            );
        }

        Basket basket = basketService.getBasketByUserId(loggedInUserId);
        if (basket == null) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no basket found"
            );
        }

        if (deleteProductDto.getQuantity() > basket.getProductList().get(productId)) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "quantity can not be greater than the quantity in basket"
            );
        }

        String token = jwtToken.getToken(SECRET_KEY).getJwtToken();
        String uri = PRODUCT_SERVICE_URI + "getProductById/" + productId;

        ProductDto productDto = appUtil.sendRequest(Request.Get(uri), token, ProductDto.class);
        if (productDto == null) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no product found"
            );
        }

        basketService.deleteProductFromBasket(basket, deleteProductDto);
        return new APIResponse(
                HttpStatus.OK,
                "success"
        );
    }

    @Procedure("this is to get the basket with given id")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getBasketById/{id}")
    public APIResponse getBasketById(@PathVariable UUID id) {
        if (id == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "basket id can not be null"
            );
        }

        Basket basket = basketService.getBasketById(id);
        if (basket == null) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no basket found"
            );
        }

        BasketDto basketDto = customMapper.map(basket, BasketDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                basketDto
        );
    }

    @Procedure("this is to get basket of the user with given user id")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getBasketByUserId/{userId}")
    public APIResponse getBasketByUserId(@PathVariable UUID userId) {
        if (userId == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "user id can not be null"
            );
        }

        Basket basket = basketService.getBasketByUserId(userId);
        if (basket == null) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no basket found"
            );
        }

        BasketDto basketDto = customMapper.map(basket, BasketDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                basketDto
        );
    }

    @Procedure("this is to create a basket")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/createBasket")
    public APIResponse createBasket(@RequestBody CreateBasketDto createBasketDto) {
        if (createBasketDto.getUserId() == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "user id can not be null"
            );
        }

        Basket basket = basketService.createOrUpdateBasket(customMapper.map(createBasketDto, Basket.class));
        BasketDto basketDto = customMapper.map(basket, BasketDto.class);
        return new APIResponse(
                HttpStatus.CREATED,
                "success",
                basketDto
        );
    }

    @Procedure("this is to update the basket with given id. If you want to clear the basket, give an empty Map<UUID,Integer>")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updateBasket")
    public APIResponse updateBasket(@RequestBody UpdateBasketDto updateBasketDto) {
        if (updateBasketDto.getBasketId() == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "basket id can not be null"
            );
        }

        Basket basket = basketService.getBasketById(updateBasketDto.getBasketId());
        if (basket == null) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no basket found"
            );
        }

        //if admin wants to clear the basket
        if (updateBasketDto.getProductList().isEmpty()) {
            basketService.clearBasket(basket);

            return new APIResponse(
                    HttpStatus.NO_CONTENT,
                    "success"
            );
        }

        basket.setProductList(updateBasketDto.getProductList());
        basketService.createOrUpdateBasket(basket);

        return new APIResponse(
                HttpStatus.NO_CONTENT,
                "success"
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/addProductToBasket")
    public APIResponse addProductToBasket(@RequestBody AddOrDeleteProductDto addProductDto) {
        if (addProductDto.getProductId() == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "product id can not be null"
            );
        }

        if (addProductDto.getQuantity() == 0) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "quantity can not be 0"
            );
        }

        Basket basket = basketService.getBasketById(addProductDto.getBasketId());
        if (basket == null) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no basket found"
            );
        }

        String token = jwtToken.getToken(SECRET_KEY).getJwtToken();
        String uri = PRODUCT_SERVICE_URI + "getProductById/" + addProductDto.getProductId();

        ProductDto productDto = appUtil.sendRequest(Request.Get(uri), token, ProductDto.class);
        if (productDto == null) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no product found"
            );
        }

        if (productDto.getStock() == 0) {
            return new APIResponse(
                    HttpStatus.OK,
                    "product not in stock"
            );
        }

        basketService.addProductToBasket(basket, addProductDto);
        return new APIResponse(
                HttpStatus.OK,
                "success"
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/deleteProductFromBasket")
    public APIResponse deleteProductFromBasket(@RequestBody AddOrDeleteProductDto deleteProductDto) {
        UUID productID = deleteProductDto.getProductId();
        if (productID == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "product id can not be null"
            );
        }

        if (deleteProductDto.getBasketId() == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "basket id can not be null"
            );
        }

        if (deleteProductDto.getQuantity() == 0) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "quantity can not be 0"
            );
        }

        Basket basket = basketService.getBasketById(deleteProductDto.getBasketId());
        if (basket == null) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no basket found"
            );
        }

        if (!basket.getProductList().containsKey(productID)) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "there is no such product in the basket"
            );
        }

        //if given product's quantity is more than the quantity in the basket
        if (deleteProductDto.getQuantity() > basket.getProductList().get(productID)) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "quantity can not be greater than the quantity in the basket"
            );
        }

        String token = jwtToken.getToken(SECRET_KEY).getJwtToken();
        String uri = PRODUCT_SERVICE_URI + "getProductById/" + productID;

        ProductDto productDto = appUtil.sendRequest(Request.Get(uri), token, ProductDto.class);
        if (productDto == null) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no product found"
            );
        }

        basketService.deleteProductFromBasket(basket, deleteProductDto);
        return new APIResponse(
                HttpStatus.OK,
                "success"
        );
    }
}
