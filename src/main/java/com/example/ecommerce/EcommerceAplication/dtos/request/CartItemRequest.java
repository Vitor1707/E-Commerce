package com.example.ecommerce.EcommerceAplication.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemRequest {
    @NotNull(message = "id do user é obrigatório")
    @Min(value = 1, message = "id do user não pode ser zero ou negativo")
    private Long idUser;

    @NotNull(message = "id do product é obrigatório")
    @Min(value = 1, message = "id do product não pode ser zero ou negativo")
    private Long idProduct;

    @NotNull(message = "quantity é obrigatório")
    @Min(value = 1, message = "quantity não pode ser zero nem um valor negativo")
    private Integer quantity;
}