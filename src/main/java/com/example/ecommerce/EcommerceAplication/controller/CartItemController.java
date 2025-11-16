package com.example.ecommerce.EcommerceAplication.controller;

import com.example.ecommerce.EcommerceAplication.dtos.request.CartItemRequest;
import com.example.ecommerce.EcommerceAplication.dtos.response.CartItemResponse;
import com.example.ecommerce.EcommerceAplication.dtos.update.CartItemUpdateRequest;
import com.example.ecommerce.EcommerceAplication.services.CartItemService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart_items")
public class CartItemController {

    private final CartItemService cartItemService;

    public CartItemController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @PostMapping
    public ResponseEntity<CartItemResponse> addToCart(@RequestBody @Valid CartItemRequest request) {
        CartItemResponse response = cartItemService.addToCart(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{idUser}")
    public ResponseEntity<Page<CartItemResponse>> cartItemsPaginated(
            @PathVariable Long idUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<CartItemResponse> responses = cartItemService.getCart(idUser, pageable);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/update_quantity")
    public ResponseEntity<CartItemResponse> updateQuantityCart(@RequestBody @Valid CartItemUpdateRequest request) {
        CartItemResponse response = cartItemService.updateQuantityCart(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/remove_quantity")
    public ResponseEntity<CartItemResponse> removeQuantityFromCart(@RequestBody @Valid CartItemUpdateRequest request) {
        CartItemResponse response = cartItemService.removeQuantityFromCart(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/user/{idUser}/remove/{idProduct}")
    public ResponseEntity<Void> removeProductFromCart(@PathVariable Long idUser, @PathVariable Long idProduct) {
        cartItemService.removeProductFromCart(idUser, idProduct);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{idUser}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable Long idUser) {
        cartItemService.clearCart(idUser);
        return ResponseEntity.noContent().build();
    }
}