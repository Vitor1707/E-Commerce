package com.example.ecommerce.EcommerceAplication.dtos.updates;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductUpdateRequest {
    @Size(min = 3, max = 50, message = "name deve ter entre 3 e 50 caracteres")
    private String name;

    @Min(value = 0)
    private BigDecimal price;

    @Size(min = 3, max = 500, message = "description deve ter entre 3 e 500 caracteres")
    private String description;

    @Min(value = 1)
    private Integer stockQuantity;
}