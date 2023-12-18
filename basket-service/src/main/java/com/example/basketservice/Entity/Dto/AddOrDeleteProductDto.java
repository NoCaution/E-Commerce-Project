package com.example.basketservice.Entity.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddOrDeleteProductDto {

    private UUID productId;

    private UUID basketId;

    private Integer quantity;

}
