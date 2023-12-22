package com.example.commonservice.Entity.Dto;

import com.example.commonservice.Entity.Enum.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private UUID id;

    private Map<UUID, Integer> productList;

    private UUID userId;

    private Long totalPrice;

    private OrderStatus orderStatus;
}
