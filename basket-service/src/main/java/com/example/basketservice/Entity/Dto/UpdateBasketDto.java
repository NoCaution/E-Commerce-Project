package com.example.basketservice.Entity.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBasketDto {

    private UUID basketId;

    private Map<UUID,Integer> productList;

}
