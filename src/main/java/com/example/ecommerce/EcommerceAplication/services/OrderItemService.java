package com.example.ecommerce.EcommerceAplication.services;

import com.example.ecommerce.EcommerceAplication.dtos.response.CartItemResponse;
import com.example.ecommerce.EcommerceAplication.exceptions.ConflictException;
import com.example.ecommerce.EcommerceAplication.exceptions.ResourceNotFoundException;
import com.example.ecommerce.EcommerceAplication.model.Order;
import com.example.ecommerce.EcommerceAplication.model.OrderItem;
import com.example.ecommerce.EcommerceAplication.model.Product;
import com.example.ecommerce.EcommerceAplication.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.security.Provider;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderItemService {

    private final ProductRepository productRepository;

    public OrderItemService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<OrderItem> createListOfOrderItem(Order order, List<CartItemResponse> cartItems) {
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
        return orderItems;
    }
}