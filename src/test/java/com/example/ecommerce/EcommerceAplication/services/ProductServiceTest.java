package com.example.ecommerce.EcommerceAplication.services;

import com.example.ecommerce.EcommerceAplication.dtos.request.ProductRequest;
import com.example.ecommerce.EcommerceAplication.dtos.response.ProductResponse;
import com.example.ecommerce.EcommerceAplication.dtos.update.ProductUpdateRequest;
import com.example.ecommerce.EcommerceAplication.exceptions.ConflictException;
import com.example.ecommerce.EcommerceAplication.exceptions.ResourceNotFoundException;
import com.example.ecommerce.EcommerceAplication.model.Category;
import com.example.ecommerce.EcommerceAplication.model.Product;
import com.example.ecommerce.EcommerceAplication.repositories.CategoryRepository;
import com.example.ecommerce.EcommerceAplication.repositories.ProductRepository;
import net.bytebuddy.TypeCache;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Category category = new Category();

    private Product product1 = new Product();

    private Product product2 = new Product();

    private ProductRequest request = new ProductRequest();

    private ProductUpdateRequest updateRequest = new ProductUpdateRequest();

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        product1 = new Product(1L, "Product1", BigDecimal.valueOf(100), 100, "Product Description", LocalDateTime.now(), LocalDateTime.now(), category, new ArrayList<>(), new ArrayList<>());
        product2 = new Product(2L, "Product2", BigDecimal.valueOf(100), 100, "Product Description", LocalDateTime.now(), LocalDateTime.now(), category, new ArrayList<>(), new ArrayList<>());
        request = new ProductRequest("Novo Product", BigDecimal.valueOf(500), 100, "Novo Product Description", "Category");
        updateRequest = new ProductUpdateRequest("Product Atualizado", BigDecimal.valueOf(500), 100, "Description Atualizada", "Category");
        category = new Category(1L, "Category", "Category Description", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>());
        pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id"));
    }

    @Test
    void deveRetornarOsProductsPaginadosSeProductsExistirem() {
        Page<Product> productsMock = new PageImpl<>(List.of(product1, product2));

        when(productRepository.findAll(pageable)).thenReturn(productsMock);

        Page<ProductResponse> result = productService.productsPaginated(pageable);

        assertNotNull(result);
        assertEquals("Product1", result.getContent().get(0).getName());
        assertEquals("Product2", result.getContent().get(1).getName());

        verify(productRepository).findAll(pageable);
    }

    @Test
    void deveRetornarUmaPageVaziaSeProductsNaoExistirem() {
        Page<Product> productsMock = new PageImpl<>(List.of());

        when(productRepository.findAll(pageable)).thenReturn(productsMock);

        Page<ProductResponse> result = productService.productsPaginated(pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(productRepository).findAll(pageable);
    }

    @Test
    void deveCriarUmProductSeTodosOsDadosForemValidos() {
        when(productRepository.existsByName(request.getName())).thenReturn(false);
        when(categoryRepository.existsByNameIgnoreCase(request.getCategory().trim().toLowerCase())).thenReturn(true);
        when(categoryRepository.findByNameIgnoreCase(request.getCategory().trim().toLowerCase())).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(new Product(1L, "Novo Product", BigDecimal.valueOf(500), 100, "Product Description", LocalDateTime.now(), LocalDateTime.now(), category, new ArrayList<>(), new ArrayList<>()));

        ProductResponse result = productService.createProduct(request);

        assertNotNull(result);
        assertEquals("Novo Product", result.getName());
        assertEquals(BigDecimal.valueOf(500), result.getPrice());

        verify(productRepository).existsByName(request.getName());
        verify(categoryRepository).existsByNameIgnoreCase(request.getCategory().trim().toLowerCase());
        verify(categoryRepository).findByNameIgnoreCase(request.getCategory().trim().toLowerCase());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void deveLancarExcecaoAoCriarProductENameJaEstiverEmUso() {
        when(productRepository.existsByName(request.getName())).thenReturn(true);

        ConflictException conflict = assertThrows(ConflictException.class, () -> productService.createProduct(request));

        assertTrue(conflict.getMessage().contains("name"));

        verify(productRepository).existsByName(request.getName());
    }

    @Test
    void deveLancarExcecaoAoCriarProductECategoryNameNaoExistir() {
        when(productRepository.existsByName(request.getName())).thenReturn(false);
        when(categoryRepository.existsByNameIgnoreCase(request.getCategory().trim().toLowerCase())).thenReturn(false);

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> productService.createProduct(request));

        assertTrue(notFound.getMessage().contains("name"));

        verify(productRepository).existsByName(request.getName());
        verify(categoryRepository).existsByNameIgnoreCase(request.getCategory().trim().toLowerCase());
    }

    @Test
    void deveLancarExcecaoAoCriarProductECategoryNaoForEncontrada() {
        when(productRepository.existsByName(request.getName())).thenReturn(false);
        when(categoryRepository.existsByNameIgnoreCase(request.getCategory().trim().toLowerCase())).thenReturn(true);
        when(categoryRepository.findByNameIgnoreCase(request.getCategory().trim().toLowerCase())).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> productService.createProduct(request));

        assertTrue(notFound.getMessage().contains("name"));

        verify(productRepository).existsByName(request.getName());
        verify(categoryRepository).existsByNameIgnoreCase(request.getCategory().trim().toLowerCase());
        verify(categoryRepository).findByNameIgnoreCase(request.getCategory().trim().toLowerCase());
    }

    @Test
    void deveRetornarUmProductAoBuscarProductPorIdEDadosForemValidos() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

        ProductResponse result = productService.findProductById(1L);

        assertNotNull(result);
        assertEquals("Product1", result.getName());

        verify(productRepository).findById(1L);
    }

    @Test
    void deveLancarUmaExcecaoAoBuscarProductPorIdEIdNaoForEncontrado() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> productService.findProductById(999L));

        assertTrue(notFound.getMessage().contains("ID"));

        verify(productRepository).findById(999L);
    }

    @Test
    void deveRetornarUmProductAoBuscarProductPorNameEDadosForemValidos() {
        when(productRepository.findByNameIgnoreCase("Product1")).thenReturn(Optional.of(product1));

        ProductResponse result = productService.findProductByName("Product1");

        assertNotNull(result);
        assertEquals("Product1", result.getName());

        verify(productRepository).findByNameIgnoreCase("Product1");
    }

    @Test
    void deveLancarUmaExcecaoAoBuscarProductPorNameENameNaoForEncontrado() {
        when(productRepository.findByNameIgnoreCase("Name")).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> productService.findProductByName("Name"));

        assertTrue(notFound.getMessage().contains("name"));

        verify(productRepository).findByNameIgnoreCase("Name");
    }

    @Test
    void deveRetornarOsProductsPaginadosPorCategorySeDadosForemValidos() {
        Page<Product> productsMock = new PageImpl<>(List.of(product1, product2));

        when(categoryRepository.findByNameIgnoreCase("Category")).thenReturn(Optional.of(category));
        when(productRepository.findAllByCategory(category, pageable)).thenReturn(productsMock);

        Page<ProductResponse> result = productService.findProductByCategory("Category", pageable);

        assertNotNull(result);
        assertEquals("Product1", result.getContent().get(0).getName());
        assertEquals("Product2", result.getContent().get(1).getName());

        verify(categoryRepository).findByNameIgnoreCase("Category");
        verify(productRepository).findAllByCategory(category, pageable);
    }

    @Test
    void deveLancarUmaExcecaoAoBuscarProductsPaginadosECategoryNaoForEncontrado() {
        Page<Product> productsMock = new PageImpl<>(List.of(product1, product2));

        when(categoryRepository.findByNameIgnoreCase("Name")).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> productService.findProductByCategory("Name", pageable));

        assertTrue(notFound.getMessage().contains("name"));

        verify(categoryRepository).findByNameIgnoreCase("Name");
    }

    @Test
    void deveRetornarUmaPageDeProductsVaziaSeProductsNaoExistirem() {
        Page<Product> productsMock = new PageImpl<>(List.of());

        when(categoryRepository.findByNameIgnoreCase("Category")).thenReturn(Optional.of(category));
        when(productRepository.findAllByCategory(category, pageable)).thenReturn(productsMock);

        Page<ProductResponse> result = productService.findProductByCategory("Category", pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(categoryRepository).findByNameIgnoreCase("Category");
        verify(productRepository).findAllByCategory(category, pageable);
    }

    @Test
    void deveAtualizarProductSeTodosOsDadosForemValidos() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.existsByNameAndIdNot(updateRequest.getName(), 1L)).thenReturn(false);
        when(categoryRepository.existsByNameIgnoreCase(updateRequest.getCategory().trim().toLowerCase())).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenReturn(new Product(1L, "Product Atualizado", BigDecimal.valueOf(500), 100, "Description Atualizada", LocalDateTime.now(), LocalDateTime.now(), category, new ArrayList<>(), new ArrayList<>()));

        ProductResponse result = productService.updateProduct(1L, updateRequest);

        assertNotNull(result);
        assertEquals("Product Atualizado", result.getName());
        assertEquals(BigDecimal.valueOf(500), result.getPrice());
        assertEquals("Description Atualizada", result.getDescription());

        verify(productRepository).findById(1L);
        verify(productRepository).existsByNameAndIdNot(updateRequest.getName(), 1L);
        verify(categoryRepository).existsByNameIgnoreCase(updateRequest.getCategory().trim().toLowerCase());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void deveLancarUmaExcecaoAoAtualizarProductEIdNaoForEncontrado() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(999L, updateRequest));

        assertTrue(notFound.getMessage().contains("ID"));

        verify(productRepository).findById(999L);
    }

    @Test
    void deveLancarUmaExcecaoAoAtualizarProductENameJaEstiverEmUso() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.existsByNameAndIdNot(updateRequest.getName(), 1L)).thenReturn(true);

        ConflictException conflict = assertThrows(ConflictException.class, () -> productService.updateProduct(1L, updateRequest));

        assertTrue(conflict.getMessage().contains("name"));

        verify(productRepository).findById(1L);
        verify(productRepository).existsByNameAndIdNot(updateRequest.getName(), 1L);
    }

    @Test
    void deveLancarUmaExcecaoAoAtualizarProductECategoryNaoExistir() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.existsByNameAndIdNot(updateRequest.getName(), 1L)).thenReturn(false);
        when(categoryRepository.existsByNameIgnoreCase(updateRequest.getCategory().trim().toLowerCase())).thenReturn(false);

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(1L, updateRequest));

        assertTrue(notFound.getMessage().contains("name"));

        verify(productRepository).findById(1L);
        verify(productRepository).existsByNameAndIdNot(updateRequest.getName(), 1L);
        verify(categoryRepository).existsByNameIgnoreCase(updateRequest.getCategory().trim().toLowerCase());
    }

    @Test
    void deveRemoverUmProductSeIdForValido() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        productService.removeProduct(1L);

        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void deveLancarExcecaoAoRemoverProductEIdNaoExistir() {
        when(productRepository.existsById(999L)).thenReturn(false);

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> productService.removeProduct(999L));

        assertTrue(notFound.getMessage().contains("ID"));

        verify(productRepository).existsById(999L);
    }
}