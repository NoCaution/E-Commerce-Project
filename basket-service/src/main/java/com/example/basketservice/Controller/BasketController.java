package com.example.basketservice.Controller;

import com.example.basketservice.Entity.Dto.AddOrDeleteProductDto;
import com.example.basketservice.Entity.Dto.CreateBasketDto;
import com.example.basketservice.Entity.Dto.UpdateBasketDto;
import com.example.basketservice.Service.BasketService;
import com.example.commonservice.Entity.APIResponse;
import com.example.commonservice.Service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/basket")
public class BasketController {
    @Autowired
    private BasketService basketService;

    @Autowired
    private CommonService commonService;


    @GetMapping("/getLoggedInUserBasket")
    public APIResponse getLoggedInUserBasket() {
        UUID loggedInUserId = UUID.fromString(commonService.getLoggedInUserId());

        return basketService.getBasketByUserId(loggedInUserId);
    }


    @PostMapping("/initializeLoggedInUserBasket")
    public APIResponse initializeLoggedInUserBasket() {
        String loggedInUserId = commonService.getLoggedInUserId();
        Map<UUID, Integer> emptyItemList = new HashMap<>();

        CreateBasketDto createBasketDto = new CreateBasketDto(
                UUID.fromString(loggedInUserId),
                emptyItemList
        );

        return basketService.createBasket(createBasketDto);
    }


    @PutMapping("/clearLoggedInUserBasket")
    public APIResponse clearLoggedInUserBasket() {
        UUID loggedInUserId = UUID.fromString(commonService.getLoggedInUserId());

        return basketService.clearBasketByUserId(loggedInUserId);
    }


    @PutMapping("/addProductToLoggedInUserBasket")
    public APIResponse addProductToLoggedInUserBasket(@RequestBody AddOrDeleteProductDto addProductDto) {
        UUID loggedInUserId = UUID.fromString(commonService.getLoggedInUserId());
        //set basket id to loggedInUserBasketId
        UUID loggedInUserBasketId = basketService.getByUserId(loggedInUserId).getId();

        addProductDto.setBasketId(loggedInUserBasketId);

        return basketService.addProductToBasket(addProductDto);
    }


    @PutMapping("/deleteProductFromLoggedInUserBasket")
    public APIResponse deleteProductFromLoggedInUserBasket(@RequestBody AddOrDeleteProductDto deleteProductDto) {
        UUID loggedInUserId = UUID.fromString(commonService.getLoggedInUserId());
        //set basket id to loggedInUserBasketId
        UUID loggedInUserBasketId = basketService.getByUserId(loggedInUserId).getId();

        deleteProductDto.setBasketId(loggedInUserBasketId);

        return basketService.deleteProductFromBasket(deleteProductDto);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getBasketById/{id}")
    public APIResponse getBasketById(@PathVariable UUID id) {
        return basketService.getBasketById(id);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getBasketByUserId/{userId}")
    public APIResponse getBasketByUserId(@PathVariable UUID userId) {
        return basketService.getBasketByUserId(userId);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/createBasket")
    public APIResponse createBasket(@RequestBody CreateBasketDto createBasketDto) {
        return basketService.createBasket(createBasketDto);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updateBasket")
    public APIResponse updateBasket(@RequestBody UpdateBasketDto updateBasketDto) {
        return basketService.updateBasket(updateBasketDto);

    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/addProductToBasket")
    public APIResponse addProductToBasket(@RequestBody AddOrDeleteProductDto addProductDto) {
        return basketService.addProductToBasket(addProductDto);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/deleteProductFromBasket")
    public APIResponse deleteProductFromBasket(@RequestBody AddOrDeleteProductDto deleteProductDto) {
        return basketService.deleteProductFromBasket(deleteProductDto);
    }
}
