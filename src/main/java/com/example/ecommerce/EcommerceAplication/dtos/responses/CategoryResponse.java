package com.example.ecommerce.EcommerceAplication.dtos.responses;

import com.example.ecommerce.EcommerceAplication.dtos.from.ProductsFromCategory;
import com.example.ecommerce.EcommerceAplication.model.Category;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime createdAt;
    private List<ProductsFromCategory> products = new ArrayList<>();

    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
        this.createdAt = category.getCreatedAt();
        if(category.getProducts() != null) {
            this.products = category.getProducts()
                    .stream()
                    .map(product -> new ProductsFromCategory(product.getId(), product.getName(), product.getPrice()))
                    .collect(Collectors.toList());
        }
    }
}