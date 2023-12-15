package com.example.productservice.Entity.Dto;

import com.example.productservice.Entity.Enum.Category;
import com.example.productservice.Entity.Enum.SubCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductDto {

    private UUID id;

    private String productName;

    private Long price;

    private MultipartFile image;

    private Long stock;

    private Category category;

    private SubCategory subCategory;
}
