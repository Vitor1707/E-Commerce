package com.example.ecommerce.EcommerceAplication.dtos.from;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductsFromCategory {
    private Long id;
    private String name;
    private BigDecimal price;
}