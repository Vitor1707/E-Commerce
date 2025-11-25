package com.example.ecommerce.EcommerceAplication.services;

import com.example.ecommerce.EcommerceAplication.dtos.request.OrderRequest;
import com.example.ecommerce.EcommerceAplication.dtos.response.CartItemResponse;
import com.example.ecommerce.EcommerceAplication.dtos.response.OrderResponse;
import com.example.ecommerce.EcommerceAplication.exceptions.ConflictException;
import com.example.ecommerce.EcommerceAplication.exceptions.ResourceNotFoundException;
import com.example.ecommerce.EcommerceAplication.model.*;
import com.example.ecommerce.EcommerceAplication.repositories.OrderRepository;
import com.example.ecommerce.EcommerceAplication.repositories.ProductRepository;
import com.example.ecommerce.EcommerceAplication.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartItemService cartItemService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderItemService orderItemService;

    @InjectMocks
    private OrderService orderService;

    private User user = new User();

    private Order order = new Order();

    private Order order1 = new Order();

    private Category category = new Category();

    private Product product = new Product();

    private CartItem cartItem = new CartItem();

    private CartItem cartItem1 = new CartItem();

    private OrderItem orderItem = new OrderItem();

    private OrderItem orderItem1 = new OrderItem();

    private OrderRequest request;

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        user = new User(1L, "User", "user@email.com", List.of(Role.USER, Role.ADMIN), "pass123", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>(), new ArrayList<>());
        order = new Order(1L, orderService.sumTotalAmount(new ArrayList<>()), OrderStatus.PENDING, LocalDateTime.now(), LocalDateTime.now(), user, new ArrayList<>());
        order1 = new Order(2L, orderService.sumTotalAmount(new ArrayList<>()), OrderStatus.PENDING, LocalDateTime.now(), LocalDateTime.now(), user, new ArrayList<>());
        category = new Category(1L, "Category", "Category Description", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>());
        product = new Product(1L, "Product", BigDecimal.valueOf(100), 1000, "Product Description", LocalDateTime.now(), LocalDateTime.now(), category, new ArrayList<>(), new ArrayList<>());
        cartItem = new CartItem(1L, 100, LocalDateTime.now(), user, product);
        cartItem1 = new CartItem(2L, 200, LocalDateTime.now(), user, product);
        orderItem = new OrderItem(1L, 10, BigDecimal.valueOf(100), LocalDateTime.now(), order, product);
        orderItem1 = new OrderItem(2L, 10, BigDecimal.valueOf(100), LocalDateTime.now(), order, product);
        request = new OrderRequest(1L, 1L, 100);
        pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id"));
    }

    @Test
    void deveAdicionarUmOrderComBaseNoCartItemSeDadosForemValidos() {
        CartItemResponse cartItemResponse = new CartItemResponse(cartItem);
        CartItemResponse cartItemResponse1 = new CartItemResponse(cartItem1);


        List<CartItemResponse> cartItemsMock = List.of(cartItemResponse, cartItemResponse1);
        List<OrderItem> orderItemsMock = List.of(orderItem, orderItem1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemService.getAllCarts(1L)).thenReturn(cartItemsMock);
        when(orderItemService.createListOfOrderItem(any(Order.class), eq(cartItemsMock))).thenReturn(orderItemsMock);
        when(orderRepository.save(any(Order.class))).thenReturn(new Order(1L, orderService.sumTotalAmount(orderItemsMock), OrderStatus.PENDING, LocalDateTime.now(), LocalDateTime.now(), user, new ArrayList<>()));
        doNothing().when(cartItemService).clearCart(1L);

        OrderResponse result = orderService.addOrderFromCart(1L);

        assertNotNull(result);
        assertEquals(orderService.sumTotalAmount(orderItemsMock), result.getTotalAmount());
        assertEquals(OrderStatus.PENDING, result.getStatus());

        verify(userRepository).findById(1L);
        verify(cartItemService).getAllCarts(1L);
        verify(orderItemService).createListOfOrderItem(any(Order.class), eq(cartItemsMock));
        verify(orderRepository).save(any(Order.class));
        verify(cartItemService).clearCart(1L);
    }

    @Test
    void deveLancarExcecaoAoAdicionarOrderComBaseNoCartItemEUserNaoExistir() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> orderService.addOrderFromCart(999L));

        assertTrue(notFound.getMessage().contains("ID"));

        verify(userRepository).findById(999L);
    }

    @Test
    void devePararOMetodoAoAdicionarOrderComBaseNoCartItemECartItemEstiverVazio() {
        List<CartItemResponse> cartItemsMock = List.of();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemService.getAllCarts(1L)).thenReturn(cartItemsMock);

        ConflictException conflict = assertThrows(ConflictException.class, () -> orderService.addOrderFromCart(1L));

        assertTrue(conflict.getMessage().contains("Cart Item"));

        verify(userRepository).findById(1L);
        verify(cartItemService).getAllCarts(1L);
    }

    @Test
    void deveAdicionarOrderAtravesDoProductSeDadosForemValidos() {
        List<OrderItem> orderItemsMock = List.of(orderItem);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(new Product(1L, "Product", BigDecimal.valueOf(100), (product.getStockQuantity() - request.getQuantity()), "Product Description", LocalDateTime.now(), LocalDateTime.now(), category, new ArrayList<>(), new ArrayList<>()));
        when(orderRepository.save(any(Order.class))).thenReturn(new Order(1L, orderService.sumTotalAmount(orderItemsMock), OrderStatus.PENDING, LocalDateTime.now(), LocalDateTime.now(), user, new ArrayList<>()));

        OrderResponse result = orderService.addOrderFromProduct(request);

        assertNotNull(result);
        assertEquals(orderService.sumTotalAmount(orderItemsMock), result.getTotalAmount());
        assertEquals(OrderStatus.PENDING, result.getStatus());

        verify(userRepository).findById(1L);
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void deveLancarExcecaoAoAdicionarOrderPeloProductEUserNaoExistir() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> orderService.addOrderFromProduct(request));

        assertTrue(notFound.getMessage().contains("ID"));

        verify(userRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoAoAdicionarOrderPeloProductEProductNaoExistir() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> orderService.addOrderFromProduct(request));

        assertTrue(notFound.getMessage().contains("ID"));

        verify(userRepository).findById(1L);
        verify(productRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoAoAdicionarOrderPeloProductEEstoqueForInsuficiente() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(new Product(1L,  "Product", BigDecimal.valueOf(100), 97, "Product Description", LocalDateTime.now(), LocalDateTime.now(), category, new ArrayList<>(), new ArrayList<>())));

        ConflictException conflict = assertThrows(ConflictException.class, () -> orderService.addOrderFromProduct(request));

        assertTrue(conflict.getMessage().contains("Estoque insuficiente"));

        verify(userRepository).findById(1L);
        verify(productRepository).findById(1L);
    }

    @Test
    void deveRetornarAOrderComBaseNoSeuIdSeDadosForemValidos() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals(orderService.sumTotalAmount(new ArrayList<>()), result.getTotalAmount());
        assertEquals(OrderStatus.PENDING, result.getStatus());

        verify(orderRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoAoBuscarOrderPorIdEIdNaoForEncontrado() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(999L));

        verify(orderRepository).findById(999L);
    }

    @Test
    void deveRetornarUmaOrderAoBuscarPorUserEDadosForemValidos(){
        Page<Order> ordersMock = new PageImpl<>(List.of(order, order1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findAllByUser(user, pageable)).thenReturn(ordersMock);

        Page<OrderResponse> result = orderService.getOrderByUser(1L, pageable);

        assertNotNull(result);
        assertEquals(orderService.sumTotalAmount(new ArrayList<>()), result.getContent().get(0).getTotalAmount());
        assertEquals(orderService.sumTotalAmount(new ArrayList<>()), result.getContent().get(1).getTotalAmount());
        assertEquals(OrderStatus.PENDING, result.getContent().get(0).getStatus());
        assertEquals(OrderStatus.PENDING, result.getContent().get(1).getStatus());

        verify(userRepository).findById(1L);
        verify(orderRepository).findAllByUser(user, pageable);
    }

    @Test
    void deveLancarExcecaoAoBuscarOrderPorUserEUserNaoForEncontrado() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderByUser(999L, pageable));

        assertTrue(notFound.getMessage().contains("ID"));

        verify(userRepository).findById(999L);
    }

    @Test
    void deveRetornarUmaPAgeVaziaAoBuscarOrderPorUserEOrdersNaoExistirem() {
        Page<Order> ordersMock = new PageImpl<>(List.of());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findAllByUser(user, pageable)).thenReturn(ordersMock);

        Page<OrderResponse> result = orderService.getOrderByUser(1L, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findById(1L);
        verify(orderRepository).findAllByUser(user, pageable);
    }

    @Test
    void deveAtualizarOrderSeDadosForemValidos() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(new Order(1L, orderService.sumTotalAmount(new ArrayList<>()), OrderStatus.CONFIRMED, LocalDateTime.now(), LocalDateTime.now(), user, new ArrayList<>()));

        OrderResponse result = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        assertNotNull(result);
        assertEquals(orderService.sumTotalAmount(new ArrayList<>()), result.getTotalAmount());
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarOrderEIdDoOrderNaoExistir() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> orderService.updateOrderStatus(999L, OrderStatus.CONFIRMED));

        assertTrue(notFound.getMessage().contains("ID"));

        verify(orderRepository).findById(999L);
    }

    @Test
    void deveCancelarORderCasoDadosForemValidos() {
        List<OrderItem> orderItemsMock = List.of(orderItem, orderItem1);

        Order orderWithItems = new Order(1L, orderService.sumTotalAmount(orderItemsMock), OrderStatus.PENDING, LocalDateTime.now(), LocalDateTime.now(), user, orderItemsMock);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(orderWithItems));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenReturn(new Order(1L, orderService.sumTotalAmount(orderItemsMock), OrderStatus.CANCELLED, LocalDateTime.now(), LocalDateTime.now(), user, orderItemsMock));

        OrderResponse result = orderService.clearOrder(1L);

        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());

        verify(orderRepository).findById(1L);
        verify(productRepository, times(2)).save(any(Product.class));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void deveLancarExcecaoAoCancelarOrderEIdDoOrderNaoExistir() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> orderService.clearOrder(999L));

        assertTrue(notFound.getMessage().contains("ID"));

        verify(orderRepository).findById(999L);
    }
}