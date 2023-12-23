package com.example.orderservice.Entity;

import com.example.commonservice.Entity.BaseModel;
import com.example.commonservice.Entity.Enum.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order extends BaseModel {

    @Column(name = "productList")
    @ElementCollection
    private Map<UUID, Integer> productList;

    @Column(nullable = false)
    private UUID userId;

    private Long totalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.NEW_ORDER;

}
