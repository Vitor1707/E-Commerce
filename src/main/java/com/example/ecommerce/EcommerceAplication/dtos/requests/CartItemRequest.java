package com.example.ecommerce.EcommerceAplication.dtos.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemRequest {
    @NotNull(message = "user é obrigatório")
    @Min(value = 1, message = "zero ou números negativos são inválidos")
    private Long idUser;
    @NotNull(message = "product é obrigatório")
    @Min(value = 1, message = "zero ou números negativos são inválidos")
    private Long idProduct;
    @NotNull(message = "quantity é obrigatória")
    @Min(value = 1, message = "zero ou números negativos são inválidos")
    private Integer quantity;
}