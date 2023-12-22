package com.example.basketservice.Entity.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBasketDto {

    private UUID userId;

    private Map<UUID,Integer> productList;
}
