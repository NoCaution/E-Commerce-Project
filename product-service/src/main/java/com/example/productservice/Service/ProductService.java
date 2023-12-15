package com.example.productservice.Service;

import com.example.productservice.Entity.Dto.CategoryDto;
import com.example.productservice.Entity.Dto.UpdateProductDto;
import com.example.productservice.Entity.Enum.Category;
import com.example.productservice.Entity.Product;
import com.example.productservice.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;


    public Product getProductById(UUID id) {
        return productRepository.findById(id).orElse(null);
    }

    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategory(Category category) {
        return productRepository.findProductsByCategory(category);
    }

    public List<Product> getProductsByCategory(CategoryDto categoryDto) {
        return productRepository.findProductsByCategoryAndSubCategory(categoryDto.getCategory(), categoryDto.getSubCategory());
    }

    public void createOrUpdateProduct(Product product) {
        productRepository.save(product);
    }

    public void deleteProduct(UUID id) {
        productRepository.deleteById(id);
    }

    public Product updateProductFieldsWithNullSafety(Product product, UpdateProductDto updateProductDto) throws IOException {
        product.setProductName(updateProductDto.getProductName() == null ? product.getProductName() : updateProductDto.getProductName());
        product.setPrice(updateProductDto.getPrice() == null ? product.getPrice() : updateProductDto.getPrice());
        product.setImage(updateProductDto.getImage() == null ? product.getImage() : updateProductDto.getImage().getBytes());
        product.setStock(updateProductDto.getStock() == null ? product.getStock() : updateProductDto.getStock());
        product.setCategory(updateProductDto.getCategory() == null ? product.getCategory() : updateProductDto.getCategory());
        product.setSubCategory(updateProductDto.getSubCategory() == null ? product.getSubCategory() : updateProductDto.getSubCategory());
        product.setUpdatedAt(new Date());
        return product;
    }
}
