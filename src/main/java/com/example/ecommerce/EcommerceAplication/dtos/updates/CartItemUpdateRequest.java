package com.example.ecommerce.EcommerceAplication.dtos.updates;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemUpdateRequest {
    @Min(value = 1, message = "zero ou números negativos são inválidos")
    private Long idUser;
    @Min(value = 1, message = "zero ou números negativos são inválidos")
    private Long idProduct;
    @Min(value = 1, message = "zero ou números negativos são inválidos")
    private Integer quantity;
}