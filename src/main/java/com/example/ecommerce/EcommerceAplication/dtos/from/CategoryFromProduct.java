package com.example.ecommerce.EcommerceAplication.dtos.from;

import com.example.ecommerce.EcommerceAplication.model.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryFromProduct {
    private Long id;
    private String name;

    public CategoryFromProduct(Category category) {
        this.id = category.getId();
        this.name = category.getName();
    }
}