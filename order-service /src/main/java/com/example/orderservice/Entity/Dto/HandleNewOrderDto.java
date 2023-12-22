package com.example.orderservice.Entity.Dto;

import com.example.commonservice.Entity.Enum.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HandleNewOrderDto {

    private UUID orderId;

    private OrderStatus orderStatus;
}
