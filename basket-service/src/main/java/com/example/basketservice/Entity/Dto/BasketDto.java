package com.example.basketservice.Entity.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketDto {

    private UUID id;

    private UUID userId;

    private Map<UUID,Integer> itemList;

    private Date createdAt;

    private Date updatedAt;
}
