package com.example.ecommerce.EcommerceAplication.services;

import com.example.ecommerce.EcommerceAplication.dtos.request.CartItemRequest;
import com.example.ecommerce.EcommerceAplication.dtos.response.CartItemResponse;
import com.example.ecommerce.EcommerceAplication.dtos.update.CartItemUpdateRequest;
import com.example.ecommerce.EcommerceAplication.exceptions.ResourceNotFoundException;
import com.example.ecommerce.EcommerceAplication.model.*;
import com.example.ecommerce.EcommerceAplication.repositories.CartItemRepository;
import com.example.ecommerce.EcommerceAplication.repositories.ProductRepository;
import com.example.ecommerce.EcommerceAplication.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.web.server.ui.OneTimeTokenSubmitPageGeneratingWebFilter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CartItemServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartItemService cartItemService;

    private User user = new User();

    private Product product = new Product();

    private Category category = new Category();

    private CartItemRequest request = new CartItemRequest();

    private CartItemUpdateRequest updateRequest = new CartItemUpdateRequest();

    private CartItemUpdateRequest updateRequestRemove = new CartItemUpdateRequest();

    private CartItem cartItem = new CartItem();

    private CartItem cartItem1 = new CartItem();

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        user = new User(1L, "User", "user@email.com", List.of(Role.USER, Role.ADMIN), "pass123", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>(), new ArrayList<>());
        category = new Category(1L, "Category", "Category Description", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>());
        product = new Product(1L, "Product", BigDecimal.valueOf(100), 1000, "Product Description", LocalDateTime.now(), LocalDateTime.now(), category, new ArrayList<>(), new ArrayList<>());
        cartItem = new CartItem(1L, 100, LocalDateTime.now(), user, product);
        cartItem1 = new CartItem(2L, 200, LocalDateTime.now(), user, product);
        request = new CartItemRequest(1L, 1L, 100);
        updateRequest = new CartItemUpdateRequest(1L, 1L, 300);
        updateRequestRemove = new CartItemUpdateRequest(1L, 1L, 50);
        pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id"));
    }

    @Test
    void deveAdicionarItemAoCarrinhoCasoDadosForemValidos() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(new CartItem(1L, 100, LocalDateTime.now(), user, product));

        CartItemResponse result = cartItemService.addToCart(request);

        assertNotNull(result);
        assertEquals(100, result.getQuantity());

        verify(userRepository).findById(1L);
        verify(productRepository).findById(1L);
        verify(cartItemRepository).findByUserAndProduct(user, product);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void deveLancarExcecaoAoAdicionarItemAoCarrinhoEIdDoUserNaoForEncontrado() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> cartItemService.addToCart(request));

        assertTrue(notFound.getMessage().contains("ID"));

        verify(userRepository).findById(1L);
        }

    @Test
    void deveLancarExcecaoAoAdicionarItemAoCarrinhoEIdDoProductNaoForEncontrado() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> cartItemService.addToCart(request));

        assertTrue(notFound.getMessage().contains("ID"));

        verify(userRepository).findById(1L);
        verify(productRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoAoAdicionarItemAoCarrinhoEUserEPRoductDoCartItemNaoForemEncontrados() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());

        assertThrows(NullPointerException.class, () -> cartItemService.addToCart(request));

        verify(userRepository).findById(1L);
        verify(productRepository).findById(1L);
        verify(cartItemRepository).findByUserAndProduct(user, product);
    }

    @Test
    void deveListarOsCartItemsSeCartItemsExistirem() {
        List<CartItem> cartItemsMock = List.of(cartItem, cartItem1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemRepository.findAllByUser(user)).thenReturn(cartItemsMock);

        List<CartItemResponse> result = cartItemService.getAllCarts(1L);

        assertNotNull(result);
        assertEquals(100, result.get(0).getQuantity());
        assertEquals(200, result.get(1).getQuantity());

        verify(userRepository).findById(1L);
        verify(cartItemRepository).findAllByUser(user);
    }

    @Test
    void deveVoltarUmaListaVaziaSeCartItemsNaoExistirem() {
        List<CartItem> cartItemsMock = List.of();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemRepository.findAllByUser(user)).thenReturn(cartItemsMock);

        List<CartItemResponse> result = cartItemService.getAllCarts(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findById(1L);
        verify(cartItemRepository).findAllByUser(user);
    }

    @Test
    void deveLancarExcecaoAoListarItemsDoCarrinhoPorUserEIdDoUserNaoForEncontrado() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> cartItemService.getAllCarts(999L));

        assertTrue(notFound.getMessage().contains("ID"));

        verify(userRepository).findById(999L);
    }

    @Test
    void deveRetonrarItemsDoCarrinhoPaginadosSeItemsExistirem() {
        Page<CartItem> cartItemsMock = new PageImpl<>(List.of(cartItem, cartItem1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemRepository.findAllByUser(user, pageable)).thenReturn(cartItemsMock);

        Page<CartItemResponse> result = cartItemService.getCart(1L, pageable);

        assertNotNull(result);
        assertEquals(100, result.getContent().get(0).getQuantity());
        assertEquals(200, result.getContent().get(1).getQuantity());

        verify(userRepository).findById(1L);
        verify(cartItemRepository).findAllByUser(user, pageable);
    }

    @Test
    void deveVoltarUmaPageDeItemsDoCarrinhoVaziaSeItemsNaoExistirem() {
        Page<CartItem> cartItemsMock = new PageImpl<>(List.of());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemRepository.findAllByUser(user, pageable)).thenReturn(cartItemsMock);

        Page<CartItemResponse> result = cartItemService.getCart(1L, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findById(1L);
        verify(cartItemRepository).findAllByUser(user, pageable);
    }

    @Test
    void deveLancarExcecaoAoBuscarItemsDoCarrinhosPaginadosPorUSerEIdDoUserNaoForEncontrado() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> cartItemService.getCart(999L, pageable));

        assertTrue(notFound.getMessage().contains("ID"));

        verify(userRepository).findById(999L);
    }

    @Test
    void deveAumentarAQuantidadeDeItensNoCarrinhoCasoDadosForemValidos() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserAndProduct(user, product)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(new CartItem(1L, 500, LocalDateTime.now(), user, product));

        CartItemResponse result = cartItemService.updateQuantityCart(updateRequest);

        assertNotNull(result);
        assertEquals(500, result.getQuantity());

        verify(userRepository).findById(1L);
        verify(productRepository).findById(1L);
        verify(cartItemRepository).findByUserAndProduct(user, product);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void deveLancarExcecaoAoAumentarAquantidadeDeitensNoCarrinhoEIdDoUserNaoForEncontrado() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> cartItemService.updateQuantityCart(updateRequest));

        assertTrue(notFound.getMessage().contains("ID"));

        verify(userRepository).findById(1L);

    }

    @Test
    void deveLancarExcecaoAoAumentarAquantidadeDeitensNoCarrinhoEIdDoProductNaoForEncontrado() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> cartItemService.updateQuantityCart(updateRequest));

        assertTrue(notFound.getMessage().contains("ID"));

        verify(userRepository).findById(1L);
        verify(productRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoAoAumentarAquantidadeDeitensNoCarrinhoECartItemNaoForEncontrado() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> cartItemService.updateQuantityCart(updateRequest));

        assertTrue(notFound.getMessage().contains("Cart Item"));

        verify(userRepository).findById(1L);
        verify(productRepository).findById(1L);
        verify(cartItemRepository).findByUserAndProduct(user, product);
    }

    @Test
    void deveRemoverQuantidadeDeItensNoCarrinhoSeDadosForemValidos() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserAndProduct(user, product)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(new CartItem(1L, 50, LocalDateTime.now(), user, product));

        CartItemResponse result = cartItemService.removeQuantityFromCart(updateRequestRemove);

        assertNotNull(result);
        assertEquals(50, result.getQuantity());

        verify(userRepository).findById(1L);
        verify(productRepository).findById(1L);
        verify(cartItemRepository).findByUserAndProduct(user, product);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void deveLancarExcecaoAoRemoverQuantidadeDeItensNoCarrinhoEIdDoUserNaoForEncontrado() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> cartItemService.removeQuantityFromCart(updateRequestRemove));

        assertTrue(notFound.getMessage().contains("ID"));

        verify(userRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoAoRemoverQuantidadeDeItensNoCarrinhoEIdDoProductNaoForEncontrado() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> cartItemService.removeQuantityFromCart(updateRequestRemove));

        assertTrue(notFound.getMessage().contains("ID"));

        verify(userRepository).findById(1L);
        verify(productRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoAoRemoverQuantidadeDeItensNoCarrinhoECartItemNaoForEncontrado() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> cartItemService.removeQuantityFromCart(updateRequestRemove));

        assertTrue(notFound.getMessage().contains("Cart Item"));

        verify(userRepository).findById(1L);
        verify(productRepository).findById(1L);
        verify(cartItemRepository).findByUserAndProduct(user, product);
    }

    @Test
    void deveRemoverProdutoDoCarrinhoCasoDadosSejamValidos() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserAndProduct(user, product)).thenReturn(Optional.of(cartItem));
        doNothing().when(cartItemRepository).deleteById(cartItem.getId());

        cartItemService.removeProductFromCart(1L, 1L);

        verify(userRepository).findById(1L);
        verify(productRepository).findById(1L);
        verify(cartItemRepository).findByUserAndProduct(user, product);
        verify(cartItemRepository).deleteById(cartItem.getId());
    }

    @Test
    void deveLancarExcecaoAoRemoverProductDoCarrinhoEIdDoUserNaoForEncontrado() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> cartItemService.removeProductFromCart(999L, 1L));

        assertTrue(notFound.getMessage().contains("ID"));

        verify(userRepository).findById(999L);
    }

    @Test
    void deveLancarExcecaoAoRemoverProductDoCarrinhoEIdDoProductNaoForEncontrado() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> cartItemService.removeProductFromCart(1L, 999L));

        assertTrue(notFound.getMessage().contains("ID"));

        verify(userRepository).findById(1L);
        verify(productRepository).findById(999L);
    }

    @Test
    void deveLancarExcecaoAoRemoverProductDoCarrinhoEItemDoCarrinhoNaoForEncontrado() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> cartItemService.removeProductFromCart(1L, 1L));

        assertTrue(notFound.getMessage().contains("Cart Item"));

        verify(userRepository).findById(1L);
        verify(productRepository).findById(1L);
        verify(cartItemRepository).findByUserAndProduct(user, product);
    }

    @Test
    void deveLimparOCarrinhoDoUserSeDadosForemValidos() {
        List<CartItem> cartItemsMock = List.of(cartItem, cartItem1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemRepository.findAllByUser(user)).thenReturn(cartItemsMock);
        doNothing().when(cartItemRepository).deleteAll(cartItemsMock);

        cartItemService.clearCart(1L);

        verify(userRepository).findById(1L);
        verify(cartItemRepository).findAllByUser(user);
        verify(cartItemRepository).deleteAll(cartItemsMock);
    }

    @Test
    void deveLancarExcecaoAoLimparCarrinhoEIdDoUserNaoForEncontrado() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> cartItemService.clearCart(999L));

        assertTrue(notFound.getMessage().contains("ID"));

        verify(userRepository).findById(999L);
    }

    @Test
    void deveLimparMesmoQueAListaDeItensNoCarrinhoEstejaVazia() {
        List<CartItem> cartItemsMock = List.of();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemRepository.findAllByUser(user)).thenReturn(cartItemsMock);
        doNothing().when(cartItemRepository).deleteAll(cartItemsMock);

        cartItemService.clearCart(1L);

        verify(userRepository).findById(1L);
        verify(cartItemRepository).findAllByUser(user);
        verify(cartItemRepository).deleteAll(cartItemsMock);
    }
}