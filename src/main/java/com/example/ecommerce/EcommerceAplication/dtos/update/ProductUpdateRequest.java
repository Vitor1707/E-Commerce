package com.example.ecommerce.EcommerceAplication.dtos.update;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @Min(value = 1, message = "price não pode ser zero nem menor que zero")
    private BigDecimal price;

    @Min(value = 1, message = "stock não pode ser zero ne um valor negativo")
    private Integer stockQuantity;

    @Size(min = 3, max = 200, message = "description deve ter entre 3 e 200 caracteres")
    private String description;

    @Size(min = 3, max = 15, message = "category deve ter entre 3 e 15 caracteres")
    private String category;
}