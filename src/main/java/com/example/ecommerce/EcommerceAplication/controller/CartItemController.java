package com.example.ecommerce.EcommerceAplication.controller;

import com.example.ecommerce.EcommerceAplication.dtos.responses.CartItemResponse;
import com.example.ecommerce.EcommerceAplication.services.CartItemService;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart_item")
public class CartItemController {

    private final CartItemService cartItemService;

    public CartItemController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @GetMapping
    public ResponseEntity<Page<CartItemResponse>> cartItemsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<CartItemResponse> cartItemsPage = cartItemService.cartItemPaginated(pageable);

        return ResponseEntity.ok(cartItemsPage);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCartItem() {
        cartItemService.clearCart();
        return ResponseEntity.noContent().build();
    }
}