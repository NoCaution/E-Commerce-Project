package com.example.productservice.Repository;

import com.example.commonservice.Entity.Enum.Category;
import com.example.commonservice.Entity.Enum.SubCategory;
import com.example.productservice.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findProductsByCategory(Category category);

    List<Product> findProductsByCategoryAndSubCategory(Category category, SubCategory subCategory);
}
