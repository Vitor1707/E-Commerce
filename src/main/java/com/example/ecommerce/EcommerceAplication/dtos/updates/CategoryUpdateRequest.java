package com.example.ecommerce.EcommerceAplication.dtos.updates;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryUpdateRequest {
    @Size(min = 3, max = 15, message = "name deve ter entre 3 e 15 caracteres")
    private String name;

    @Size(min = 3, max = 100, message = "description deve ter entre 3 e 100 caracteres")
    private String description;
}