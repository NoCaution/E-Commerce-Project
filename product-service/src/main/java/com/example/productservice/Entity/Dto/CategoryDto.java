package com.example.productservice.Entity.Dto;

import com.example.productservice.Entity.Enum.Category;
import com.example.productservice.Entity.Enum.SubCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {

    private Category category;

    private SubCategory subCategory;

}
