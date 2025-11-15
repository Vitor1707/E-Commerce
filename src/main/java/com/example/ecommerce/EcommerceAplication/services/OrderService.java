package com.example.ecommerce.EcommerceAplication.services;

import com.example.ecommerce.EcommerceAplication.dtos.responses.CartItemResponse;
import com.example.ecommerce.EcommerceAplication.dtos.responses.OrderResponse;
import com.example.ecommerce.EcommerceAplication.exceptions.ConflictException;
import com.example.ecommerce.EcommerceAplication.exceptions.ResourceNotFoundException;
import com.example.ecommerce.EcommerceAplication.model.*;
import com.example.ecommerce.EcommerceAplication.repositories.OrderRepository;
import com.example.ecommerce.EcommerceAplication.repositories.ProductRepository;
import com.example.ecommerce.EcommerceAplication.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartItemService cartItemService;
    private final ProductRepository productRepository;
    private final OrderItemService orderItemService;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository, CartItemService cartItemService,
                        ProductRepository productRepository, OrderItemService orderItemService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.cartItemService = cartItemService;
        this.productRepository = productRepository;
        this.orderItemService = orderItemService;
    }

    public OrderResponse createOrder(Long idUser) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("User", idUser));

        List<CartItemResponse> cartItems = cartItemService.getCartList(idUser);

        Order order = new Order();

        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems = orderItemService.createOrderItem(order, cartItems);

        order.setTotalAmount(orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        order.setOrderItems(orderItems);

        Order orderSaved = orderRepository.save(order);
        return new OrderResponse(orderSaved);
    }

    public OrderResponse getOrderById(Long idOrder) {
        return orderRepository.findById(idOrder)
                .map(OrderResponse::new)
                .orElseThrow(() -> new ResourceNotFoundException("Order", idOrder));
    }

    public Page<OrderResponse> getOrderByUser(Long idUser, Pageable pageable) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("User", idUser));

        Page<Order> ordersPage = orderRepository.findByUser(user, pageable);
        return ordersPage.map(OrderResponse::new);
    }

    public OrderResponse updateOrder(Long idOrder, OrderStatus status) {
        Order order = orderRepository.findById(idOrder)
                .orElseThrow(() -> new ResourceNotFoundException("Order", idOrder));

        order.setStatus(status);

        Order orderUpdated = orderRepository.save(order);
        return new OrderResponse(orderUpdated);
    }

    public OrderResponse cancelOrder(Long idOrder) {
        Order order = orderRepository.findById(idOrder)
                .orElseThrow(() -> new ResourceNotFoundException("Order", idOrder));

        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order orderCancelled = orderRepository.save(order);
        return new OrderResponse(orderCancelled);
    }
}