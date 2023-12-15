package com.example.productservice.Entity;

import com.example.productservice.Entity.Enum.Category;
import com.example.productservice.Entity.Enum.SubCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private UUID id;

    private String productName;

    private Long price;

    @Lob
    private byte[] image;

    private Long stock;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private SubCategory subCategory;

    private Date createdAt;

    private Date updatedAt;

}
