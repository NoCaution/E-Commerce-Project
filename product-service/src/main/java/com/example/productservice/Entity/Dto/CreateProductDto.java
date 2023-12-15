package com.example.productservice.Entity.Dto;

import com.example.productservice.Entity.Enum.Category;
import com.example.productservice.Entity.Enum.SubCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductDto {

    private String productName;

    private Long price;

    private MultipartFile image;

    private Long stock;

    private Category category;

    private SubCategory subCategory;
}
