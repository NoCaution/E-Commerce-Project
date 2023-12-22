package com.example.orderservice.Entity;

import com.example.commonservice.Entity.Enum.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @Column(nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "productList")
    @ElementCollection
    private Map<UUID, Integer> productList;

    @Column(nullable = false)
    private UUID userId;

    private Long totalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.NEW_ORDER;

    @Column(nullable = false)
    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedAt;

}
