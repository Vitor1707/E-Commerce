package com.example.ecommerce.EcommerceAplication.services;

import com.example.ecommerce.EcommerceAplication.dtos.request.CartItemRequest;
import com.example.ecommerce.EcommerceAplication.dtos.response.CartItemResponse;
import com.example.ecommerce.EcommerceAplication.dtos.update.CartItemUpdateRequest;
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

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartItemService(CartItemRepository cartItemRepository,
                           UserRepository userRepository,
                           ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public CartItemResponse addToCart(CartItemRequest request) {
        User user = userRepository.findById(request.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getIdUser()));

        Product product = productRepository.findById(request.getIdProduct())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getIdProduct()));

        if(request.getQuantity() > product.getStockQuantity()) {
            throw new ConflictException("Estoque insuficiente");
        }

        Optional<CartItem> existingItem = cartItemRepository.findByUserAndProduct(user, product);

        if(existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            return new CartItemResponse(item);
        }

        CartItem cartItem = new CartItem();

        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(request.getQuantity());

        CartItem cartItemSaved = cartItemRepository.save(cartItem);
        return new CartItemResponse(cartItemSaved);
    }

    public List<CartItemResponse> getAllCarts(Long idUser) {
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

        Page<CartItem> cartItemsPage = cartItemRepository.findAllByUser(user, pageable);

        return cartItemsPage.map(CartItemResponse::new);
    }

    public CartItemResponse updateQuantityCart(CartItemUpdateRequest request) {
        User user = userRepository.findById(request.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getIdUser()));

        Product product = productRepository.findById(request.getIdProduct())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getIdProduct()));

        CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new ResourceNotFoundException("Cart Item com esse user ou product não foi encontrado"));

        if((request.getQuantity() + cartItem.getQuantity()) > product.getStockQuantity()) {
            throw new ConflictException("Estoque insuficente");
        }

        cartItem.setQuantity(request.getQuantity() + cartItem.getQuantity());

        CartItem cartItemUpdated = cartItemRepository.save(cartItem);
        return new CartItemResponse(cartItemUpdated);
    }

    public CartItemResponse removeQuantityFromCart(CartItemUpdateRequest request) {
        User user = userRepository.findById(request.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getIdUser()));

        Product product = productRepository.findById(request.getIdProduct())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getIdProduct()));

        CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new ResourceNotFoundException("Cart Item com esse user ou product não foi encontrado"));

        int quantity = cartItem.getQuantity() - request.getQuantity();

        if(quantity <= 0) {
            cartItemRepository.delete(cartItem);
            return null;
        }

        cartItem.setQuantity(cartItem.getQuantity() - request.getQuantity());

        CartItem cartItemUpdated = cartItemRepository.save(cartItem);
        return new CartItemResponse(cartItemUpdated);
    }

    public void removeProductFromCart(Long idUser, Long idProduct) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("User", idUser));

        Product product = productRepository.findById(idProduct)
                .orElseThrow(() -> new ResourceNotFoundException("Product", idProduct));

        CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new ResourceNotFoundException("Cart Item com esse user ou product não foi encontrado"));

        cartItemRepository.deleteById(cartItem.getId());
    }

    public void clearCart(Long idUser) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("User", idUser));

        List<CartItem> cartItem = cartItemRepository.findAllByUser(user);

        cartItemRepository.deleteAll(cartItem);
    }
}