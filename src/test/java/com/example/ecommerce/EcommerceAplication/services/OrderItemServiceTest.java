package com.example.ecommerce.EcommerceAplication.services;

import com.example.ecommerce.EcommerceAplication.dtos.response.CartItemResponse;
import com.example.ecommerce.EcommerceAplication.exceptions.ConflictException;
import com.example.ecommerce.EcommerceAplication.exceptions.ResourceNotFoundException;
import com.example.ecommerce.EcommerceAplication.model.*;
import com.example.ecommerce.EcommerceAplication.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderItemService orderItemService;

    private User user;

    private Order order;

    private OrderItem orderItem;

    private OrderItem orderItem1;

    private Category category;

    private Product product;

    private CartItem cartItem;

    private CartItem cartItem1;

    @BeforeEach
    void setUp() {
        user = new User(1L, "User", "user@email.com", List.of(Role.USER, Role.ADMIN), "pass123", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>(), new ArrayList<>());
        order = new Order(1L, orderService.sumTotalAmount(new ArrayList<>()), OrderStatus.PENDING, LocalDateTime.now(), LocalDateTime.now(), user, new ArrayList<>());
        orderItem = new OrderItem(1L, 10, BigDecimal.valueOf(100), LocalDateTime.now(), order, product);
        orderItem1 = new OrderItem(2L, 10, BigDecimal.valueOf(100), LocalDateTime.now(), order, product);
        category = new Category(1L, "Category", "Category Description", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>());
        product = new Product(1L, "Product", BigDecimal.valueOf(100), 1000, "Product Description", LocalDateTime.now(), LocalDateTime.now(), category, new ArrayList<>(), new ArrayList<>());
        cartItem = new CartItem(1L, 100, LocalDateTime.now(), user, product);
        cartItem1 = new CartItem(2L, 100, LocalDateTime.now(), user, product);
    }

    @Test
    void deveCriarUmaListaDeOrderItemsSeDadosForemValidos() {
        CartItemResponse cartItemResponse = new CartItemResponse(cartItem);
        CartItemResponse cartItemResponse1 = new CartItemResponse(cartItem1);

        List<CartItemResponse> cartItemsMock = List.of(cartItemResponse, cartItemResponse1);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        List<OrderItem> result = orderItemService.createListOfOrderItem(order, cartItemsMock);

        assertNotNull(result);
        assertEquals(100, result.get(0).getQuantity());
        assertEquals(BigDecimal.valueOf(100), result.get(0).getPrice());
        assertEquals(100, result.get(1).getQuantity());
        assertEquals(BigDecimal.valueOf(100), result.get(1).getPrice());

        verify(productRepository, times(2)).findById(1L);
        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    void DeveLancarExcecaoAoListarOrderItemEIdDoProductNaoExistir() {
        CartItemResponse cartItemResponse = new CartItemResponse(cartItem);
        CartItemResponse cartItemResponse1 = new CartItemResponse(cartItem1);

        List<CartItemResponse> cartItemsMock = List.of(cartItemResponse, cartItemResponse1);

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> orderItemService.createListOfOrderItem(order, cartItemsMock));

        assertTrue(notFound.getMessage().contains("ID"));

        verify(productRepository).findById(1L);
    }

    @Test
    void deveLancarUmaExcecaoCasoEstoqueDoProductSejaInsuficiente() {
        CartItemResponse cartItemResponse = new CartItemResponse(cartItem);
        CartItemResponse cartItemResponse1 = new CartItemResponse(cartItem1);

        List<CartItemResponse> cartItemsMock = List.of(cartItemResponse, cartItemResponse1);

        when(productRepository.findById(1L)).thenReturn(Optional.of(new Product(1L, "Product", BigDecimal.valueOf(100), 9, "Product Description", LocalDateTime.now(), LocalDateTime.now(), category, new ArrayList<>(), new ArrayList<>())));

        ConflictException conflict = assertThrows(ConflictException.class, () -> orderItemService.createListOfOrderItem(order, cartItemsMock));

        assertTrue(conflict.getMessage().contains("Estoque insuficiente"));

        verify(productRepository).findById(1L);
    }
}