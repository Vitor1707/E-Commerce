package com.example.ecommerce.EcommerceAplication.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequest {
    @NotBlank(message = "name é obrigatório")
    @Size(min = 3, max = 15, message = "name deve ter entre 3 e 15 caracteres")
    private String name;

    @Size(min = 3, max = 100, message = "description deve ter entre 3 e 100 caracteres")
    private String description;
}