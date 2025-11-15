package com.example.ecommerce.EcommerceAplication.controller;


import com.example.ecommerce.EcommerceAplication.dtos.responses.OrderResponse;
import com.example.ecommerce.EcommerceAplication.model.OrderStatus;
import com.example.ecommerce.EcommerceAplication.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@PathVariable Long idUser) {
        OrderResponse response = orderService.createOrder(idUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id){
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{idUser}")
    public ResponseEntity<Page<OrderResponse>> getOrderByUser(
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

    @PutMapping("/{idOrder}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable Long idUser, @RequestBody @Valid OrderStatus orderStatus) {
        OrderResponse response = orderService.updateOrder(idUser, orderStatus);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrderResponse> clearOrder(@PathVariable Long id) {
        OrderResponse response = orderService.cancelOrder(id);
        return ResponseEntity.ok(response);
    }
}