package com.example.ecommerce.EcommerceAplication.services;

import com.example.ecommerce.EcommerceAplication.dtos.responses.CartItemResponse;
import com.example.ecommerce.EcommerceAplication.exceptions.ConflictException;
import com.example.ecommerce.EcommerceAplication.model.CartItem;
import com.example.ecommerce.EcommerceAplication.repositories.CartItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CartItemService {

    private final CartItemRepository cartItemRepository;

    public CartItemService(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    public Page<CartItemResponse> cartItemPaginated(Pageable pageable) {
        Page<CartItem> cartItemsPage = cartItemRepository.findAll(pageable);
        return cartItemsPage.map(CartItemResponse::new);
    }

    public void clearCart() {
        if(cartItemRepository.findAll().isEmpty()) {
            throw new ConflictException("O carrinho já está vazio");
        }

        cartItemRepository.deleteAll();
    }
}