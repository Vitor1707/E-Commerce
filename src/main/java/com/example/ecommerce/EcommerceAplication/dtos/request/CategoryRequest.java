package com.example.ecommerce.EcommerceAplication.dtos.request;

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

    @Size(min = 3, max = 500, message = "description deve ter entre 3 e 500 caracteres")
    private String description;
}