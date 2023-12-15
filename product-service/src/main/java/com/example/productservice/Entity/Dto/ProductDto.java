package com.example.productservice.Entity.Dto;

import com.example.productservice.Entity.Enum.Category;
import com.example.productservice.Entity.Enum.SubCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private UUID id;

    private String productName;

    private Long price;

    private byte[] image;

    private Long stock;

    private Category category;

    private SubCategory subCategory;

}
