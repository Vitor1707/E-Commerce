package com.example.ecommerce.EcommerceAplication.services;

import com.example.ecommerce.EcommerceAplication.dtos.request.OrderRequest;
import com.example.ecommerce.EcommerceAplication.dtos.response.CartItemResponse;
import com.example.ecommerce.EcommerceAplication.dtos.response.OrderResponse;
import com.example.ecommerce.EcommerceAplication.exceptions.ConflictException;
import com.example.ecommerce.EcommerceAplication.exceptions.ResourceNotFoundException;
import com.example.ecommerce.EcommerceAplication.model.*;
import com.example.ecommerce.EcommerceAplication.repositories.CartItemRepository;
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
    private final CartItemService cartItemService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public OrderService(OrderRepository orderRepository, CartItemService cartItemService,
                        UserRepository userRepository,
                        ProductRepository productRepository,
                        CartItemRepository cartItemRepository) {
        this.orderRepository = orderRepository;
        this.cartItemService = cartItemService;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public OrderResponse addOrderFromCart(Long idUser) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("User", idUser));

        Order order = new Order();

        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        List<CartItemResponse> cartItems = cartItemService.getAllCarts(idUser);

        List<OrderItem> orderItems = new ArrayList<>();
        for(CartItemResponse cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", cartItem.getProduct().getId()));

            if(cartItem.getQuantity() > product.getStockQuantity()) {
                throw new ConflictException("Estoque insuficiente, produto: " + product.getName() +
                        ". Disponível: " + product.getStockQuantity() +
                        ", Solicitado: " + cartItem.getQuantity());
            }

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());

            orderItems.add(orderItem);
        }

        order.setTotalAmount(orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        order.setOrderItems(orderItems);

        Order orderSaved = orderRepository.save(order);

        cartItemService.clearCart(idUser);

        return new OrderResponse(orderSaved);
    }

    public OrderResponse addOrderFromProduct(OrderRequest request) {
        User user = userRepository.findById(request.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getIdUser()));

        Product product = productRepository.findById(request.getIdProduct())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getIdProduct()));

        if(request.getQuantity() > product.getStockQuantity()) {
            throw new ConflictException("Estoque insuficiente, produto: " + product.getName() +
                    ". Disponível: " + product.getStockQuantity() +
                    ", Solicitado: " + request.getQuantity());
        }

        product.setStockQuantity(product.getStockQuantity() - request.getQuantity());
        productRepository.save(product);

        Order order = new Order();

        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems = new ArrayList<>();

        OrderItem orderItem = new OrderItem();

        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setPrice(product.getPrice());
        orderItem.setQuantity(request.getQuantity());

        orderItems.add(orderItem);

        order.setTotalAmount(orderItems
                .stream()
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

        Page<Order> usersPage = orderRepository.findAllByUser(user, pageable);

        return usersPage.map(OrderResponse::new);
    }

    public OrderResponse updateOrderStatus(Long idOrder, OrderStatus status) {
        Order order = orderRepository.findById(idOrder)
                .orElseThrow(() -> new ResourceNotFoundException("Order", idOrder));

        order.setStatus(status);

        Order orderUpdated = orderRepository.save(order);
        return new OrderResponse(orderUpdated);
    }

    public OrderResponse clearOrder(Long idOrder) {
        Order order = orderRepository.findById(idOrder)
                .orElseThrow(() -> new ResourceNotFoundException("Order", idOrder));

        for(OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
            productRepository.save(product);//////
        }

        order.setStatus(OrderStatus.CANCELLED);

        Order orderUpdated = orderRepository.save(order);
        return new OrderResponse(orderUpdated);
    }
}