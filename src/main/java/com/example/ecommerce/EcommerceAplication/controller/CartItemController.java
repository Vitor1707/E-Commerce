package com.example.ecommerce.EcommerceAplication.controller;

import com.example.ecommerce.EcommerceAplication.dtos.requests.CartItemRequest;
import com.example.ecommerce.EcommerceAplication.dtos.responses.CartItemResponse;
import com.example.ecommerce.EcommerceAplication.dtos.updates.CartItemUpdateRequest;
import com.example.ecommerce.EcommerceAplication.services.CartItemService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart_item")
public class CartItemController {

    private final CartItemService cartItemService;

    public CartItemController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @GetMapping("/user/{idUser}")
    public ResponseEntity<Page<CartItemResponse>> getCar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @PathVariable Long idUser
    ) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<CartItemResponse> response = cartItemService.getCart(idUser, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CartItemResponse> addToCar(@RequestBody @Valid CartItemRequest request) {
        CartItemResponse response = cartItemService.addToCart(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/update_quantity")
    public ResponseEntity<CartItemResponse> updateQuantity(@RequestBody @Valid CartItemUpdateRequest request) {
        CartItemResponse response = cartItemService.updateQuantity(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/remove_quantity")
    public ResponseEntity<CartItemResponse> removeQuantity(@RequestBody @Valid CartItemUpdateRequest request) {
        CartItemResponse response = cartItemService.removeQuantity(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/user/{idUser}/product/{idProduct}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long idUser, @PathVariable Long idProduct) {
        cartItemService.removeFromCart(idUser, idProduct);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/id/{idUser}")
    public ResponseEntity<Void> clearCartItem(@PathVariable Long idUser) {
        cartItemService.clearCart(idUser);
        return ResponseEntity.noContent().build();
    }
}