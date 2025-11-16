package com.example.ecommerce.EcommerceAplication.dtos.from;

import com.example.ecommerce.EcommerceAplication.model.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductFromCartItem {
    private Long id;
    private String name;
    private BigDecimal price;

    public ProductFromCartItem(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
    }
}