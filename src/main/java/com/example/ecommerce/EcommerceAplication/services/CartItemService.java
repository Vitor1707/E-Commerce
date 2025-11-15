package com.example.ecommerce.EcommerceAplication.services;

import com.example.ecommerce.EcommerceAplication.dtos.requests.CartItemRequest;
import com.example.ecommerce.EcommerceAplication.dtos.responses.CartItemResponse;
import com.example.ecommerce.EcommerceAplication.dtos.updates.CartItemUpdateRequest;
import com.example.ecommerce.EcommerceAplication.exceptions.ConflictException;
import com.example.ecommerce.EcommerceAplication.exceptions.ResourceNotFoundException;
import com.example.ecommerce.EcommerceAplication.model.CartItem;
import com.example.ecommerce.EcommerceAplication.model.Product;
import com.example.ecommerce.EcommerceAplication.model.User;
import com.example.ecommerce.EcommerceAplication.repositories.CartItemRepository;
import com.example.ecommerce.EcommerceAplication.repositories.ProductRepository;
import com.example.ecommerce.EcommerceAplication.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartItemService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public CartItemService(UserRepository userRepository, ProductRepository productRepository, CartItemRepository cartItemRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public List<CartItemResponse> getCartList(Long idUser) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("User", idUser));

        return cartItemRepository.findAllByUser(user)
                .stream()
                .map(CartItemResponse::new)
                .toList();
    }

    public Page<CartItemResponse> getCart(Long idUser, Pageable pageable) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("User", idUser));

        Page<CartItem> getCartPages = cartItemRepository.findAllByUser(user, pageable);

        return getCartPages.map(CartItemResponse::new);
    }

    public CartItemResponse addToCart(CartItemRequest request) {
        User user = userRepository.findById(request.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getIdUser()));

        Product product = productRepository.findById(request.getIdProduct())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getIdProduct()));

        if(product.getStockQuantity() < request.getQuantity()) {
            throw new ConflictException("Quantidade em estoque insuficiente");
        }

        Optional<CartItem> existingItem = cartItemRepository.findByUserAndProduct(user, product);

        if(existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            return new CartItemResponse(item);
        }

        CartItem cartItem = new CartItem();

        cartItem.setQuantity(request.getQuantity());
        cartItem.setUser(user);
        cartItem.setProduct(product);

        CartItem cartItemSaved = cartItemRepository.save(cartItem);
        return new CartItemResponse(cartItemSaved);
    }

    public CartItemResponse updateQuantity(CartItemUpdateRequest request) {
        User user = userRepository.findById(request.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getIdUser()));

        Product product = productRepository.findById(request.getIdProduct())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getIdProduct()));

        CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new ResourceNotFoundException("User ou product não encontrados"));

        if(product.getStockQuantity() < (request.getQuantity() + cartItem.getQuantity())) {
            throw new ConflictException("Quantidade em estoque insuficiente");
        }

        cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());

        CartItem cartItemUpdated = cartItemRepository.save(cartItem);
        return new CartItemResponse(cartItemUpdated);
    }

    public CartItemResponse removeQuantity(CartItemUpdateRequest request) {
        User user = userRepository.findById(request.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getIdUser()));

        Product product = productRepository.findById(request.getIdProduct())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getIdProduct()));

        CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new ResourceNotFoundException("User ou product não encontrados"));

        int newQuantity = cartItem.getQuantity() - request.getQuantity();

        if(newQuantity <= 0) {
            cartItemRepository.delete(cartItem);
            return null;
        }

        cartItem.setQuantity(cartItem.getQuantity() - request.getQuantity());

        CartItem cartItemUpdated = cartItemRepository.save(cartItem);
        return new CartItemResponse(cartItemUpdated);
    }

    public void removeFromCart(Long idUser, Long idProduct) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("User", idUser));

        Product product = productRepository.findById(idProduct)
                .orElseThrow(() -> new ResourceNotFoundException("Product", idProduct));

        CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new ResourceNotFoundException("User ou Product não encontrados"));

        cartItemRepository.deleteById(cartItem.getId());
    }

    public void clearCart(Long idUser) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("User", idUser));

        List<CartItem> userCart = cartItemRepository.findAllByUser(user);

        cartItemRepository.deleteAll(userCart);
    }
}