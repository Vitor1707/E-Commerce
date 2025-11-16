package com.example.ecommerce.EcommerceAplication.dtos.response;

import com.example.ecommerce.EcommerceAplication.dtos.from.ProductFromCartItem;
import com.example.ecommerce.EcommerceAplication.dtos.from.UserFromCartItem;
import com.example.ecommerce.EcommerceAplication.model.CartItem;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {
    private Long id;
    private Integer quantity;
    private UserFromCartItem user;
    private ProductFromCartItem product;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime updatedAt;

    public CartItemResponse(CartItem cartItem) {
        this.id = cartItem.getId();
        this.quantity = cartItem.getQuantity();
        this.user = new UserFromCartItem(cartItem.getUser().getId(), cartItem.getUser().getUsername());
        this.product = new ProductFromCartItem(cartItem.getProduct().getId(), cartItem.getProduct().getName(), cartItem.getProduct().getPrice());
        this.updatedAt = cartItem.getUpdatedAt();
    }
}