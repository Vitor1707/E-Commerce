package com.example.ecommerce.EcommerceAplication.dtos.requests;

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
public class ProductRequest {
    @NotBlank(message = "name é obrigatório")
    @Size(min = 3, max = 50, message = "name deve ter entre 3 e 50 caracteres")
    private String name;

    @NotNull(message = "price é obrigatório")
    private BigDecimal price;

    @Size(min = 3, max = 500, message = "description deve ter entre 3 e 500 caracteres")
    private String description;

    @NotNull(message = "stockQuantity é obrigatório")
    @Min(value = 1)
    private Integer stockQuantity;
}