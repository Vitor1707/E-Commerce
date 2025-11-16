package com.example.ecommerce.EcommerceAplication.controller;

import com.example.ecommerce.EcommerceAplication.dtos.request.OrderRequest;
import com.example.ecommerce.EcommerceAplication.dtos.response.OrderResponse;
import com.example.ecommerce.EcommerceAplication.model.OrderStatus;
import com.example.ecommerce.EcommerceAplication.services.OrderService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/{idUser}/add_to_order")
    public ResponseEntity<OrderResponse> addToOrderFromCart(@PathVariable Long idUser) {
        OrderResponse response = orderService.addOrderFromCart(idUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/from_product")
    public ResponseEntity<OrderResponse> addToOrderFromProduct(@RequestBody @Valid OrderRequest request) {
        OrderResponse response = orderService.addOrderFromProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{idUser}")
    public ResponseEntity<Page<OrderResponse>> getOrderById(
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

        Page<OrderResponse> response = orderService.getOrderByUser(idUser, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{idOrder}/update")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long idOrder, @RequestBody @Valid OrderStatus status) {
        OrderResponse response = orderService.updateOrderStatus(idOrder, status);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{idOrder}")
    public ResponseEntity<OrderResponse> clearOrder(@PathVariable Long idOrder) {
        OrderResponse response = orderService.clearOrder(idOrder);
        return ResponseEntity.ok(response);
    }
}