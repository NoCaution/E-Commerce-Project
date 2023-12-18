package com.example.basketservice.Service;

import com.example.basketservice.Entity.Basket;
import com.example.basketservice.Entity.Dto.AddOrDeleteProductDto;
import com.example.basketservice.Repository.BasketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class BasketService {

    @Autowired
    private BasketRepository basketRepository;

    public Basket getBasketById(UUID id) {
        return basketRepository.findById(id).orElse(null);
    }

    public Basket getBasketByUserId(UUID userId) {
        return basketRepository.findBasketByUserId(userId);
    }

    public Basket createOrUpdateBasket(Basket basket) {
        return basketRepository.save(basket);
    }

    public void addProductToBasket(Basket basket, AddOrDeleteProductDto addProductDto) {
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
        createOrUpdateBasket(basket);
    }

    public void deleteProductFromBasket(Basket basket, AddOrDeleteProductDto deleteProductDto) {
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
        createOrUpdateBasket(basket);
    }

    public void clearBasket(Basket basket) {
        basket.setProductList(new HashMap<>());
        createOrUpdateBasket(basket);
    }

}
