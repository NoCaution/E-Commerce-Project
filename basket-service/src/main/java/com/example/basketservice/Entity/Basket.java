package com.example.basketservice.Entity;

import com.example.commonservice.Entity.BaseModel;
import jakarta.persistence.*;
import lombok.*;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "baskets")
public class Basket extends BaseModel {
    private UUID userId;

    @Column(name = "productList")
    @ElementCollection
    private Map<UUID,Integer> productList;
}
