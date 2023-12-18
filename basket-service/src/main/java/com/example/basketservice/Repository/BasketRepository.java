package com.example.basketservice.Repository;

import com.example.basketservice.Entity.Basket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BasketRepository extends JpaRepository<Basket, UUID> {
    Basket findBasketByUserId(UUID userId);
}
