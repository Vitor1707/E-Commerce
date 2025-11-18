package com.example.ecommerce.EcommerceAplication.services;

import com.example.ecommerce.EcommerceAplication.dtos.request.CategoryRequest;
import com.example.ecommerce.EcommerceAplication.dtos.response.CategoryResponse;
import com.example.ecommerce.EcommerceAplication.dtos.response.ProductResponse;
import com.example.ecommerce.EcommerceAplication.exceptions.ConflictException;
import com.example.ecommerce.EcommerceAplication.exceptions.ResourceNotFoundException;
import com.example.ecommerce.EcommerceAplication.model.Category;
import com.example.ecommerce.EcommerceAplication.model.Product;
import com.example.ecommerce.EcommerceAplication.repositories.CategoryRepository;
import com.example.ecommerce.EcommerceAplication.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category1 = new Category();
    private Category category2 = new Category();

    private Product product1 = new Product();
    private Product product2 = new Product();

    private CategoryRequest request = new CategoryRequest();

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        category1 = new Category(1L, "Category1", "Description", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>());
        category2 = new Category(2L, "Category2", "Description", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>());
        product1 = new Product(1L, "Product1", BigDecimal.valueOf(100), 100, "Product Description", LocalDateTime.now(), LocalDateTime.now(), category1, new ArrayList<>(), new ArrayList<>());
        product2 = new Product(2L, "Product2", BigDecimal.valueOf(100), 100, "Product Description", LocalDateTime.now(), LocalDateTime.now(), category1, new ArrayList<>(), new ArrayList<>());
        request = new CategoryRequest("Nova Category", "Description");
        pageable = PageRequest.of(0, 4, Sort.by(Sort.Direction.ASC, "id"));
    }

    @Test
    void deveCriarCategorySeTodosOsDadosForemValidos() {
        when(categoryRepository.existsByNameIgnoreCase(request.getName())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(new Category(1L, "Category", "Description", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>()));

        CategoryResponse result = categoryService.createCategory(request);

        assertNotNull(result);
        assertEquals("Category", result.getName());
        assertEquals("Description", result.getDescription());

        verify(categoryRepository).existsByNameIgnoreCase(request.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void deveLancarExcecaoAoCriarCategoryENameJaExistir() {
        when(categoryRepository.existsByNameIgnoreCase(request.getName())).thenReturn(true);

        ConflictException conflict = assertThrows(ConflictException.class, () -> categoryService.createCategory(request));

        assertTrue(conflict.getMessage().contains("name"));

        verify(categoryRepository).existsByNameIgnoreCase(request.getName());
    }

    @Test
    void deveVoltarCategoriesPaginadasSeCategoriesExistirem() {
        Page<Category> categoriesMock = new PageImpl<>(List.of(category1, category2));

        when(categoryRepository.findAll(pageable)).thenReturn(categoriesMock);

        Page<CategoryResponse> result = categoryService.categoriesPaginated(pageable);

        assertNotNull(result);
        assertEquals("Category1", result.getContent().get(0).getName());
        assertEquals("Category2", result.getContent().get(1).getName());

        verify(categoryRepository).findAll(pageable);
    }

    @Test
    void deveVoltarUmaPAgeVaziaSeCategoriesNaoExistirem() {
        Page<Category> categoriesMock = new PageImpl<>(List.of());

        when(categoryRepository.findAll(pageable)).thenReturn(categoriesMock);

        Page<CategoryResponse> result = categoryService.categoriesPaginated(pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(categoryRepository).findAll(pageable);
    }

    @Test
    void deveRetornarProductsPaginadosPelaCategorySeProductsExistiremEDadosForemValidos() {
        Page<Product> productsMock = new PageImpl<>(List.of(product1, product2));

        when(categoryRepository.findByNameIgnoreCase(category1.getName().trim().toLowerCase())).thenReturn(Optional.of(category1));
        when(productRepository.findAllByCategory(category1, pageable)).thenReturn(productsMock);

        Page<ProductResponse> result = categoryService.getProductsByCategory(category1.getName(), pageable);

        assertNotNull(result);
        assertEquals("Product1", result.getContent().get(0).getName());
        assertEquals("Product2", result.getContent().get(1).getName());

        verify(categoryRepository).findByNameIgnoreCase(category1.getName().trim().toLowerCase());
        verify(productRepository).findAllByCategory(category1, pageable);
    }

    @Test
    void deveRetonrarUmaPAgeVaziaAoBuscarProductsPorCategoryEProductsNaoExistirem() {
        Page<Product> productsMock = new PageImpl<>(List.of());

        when(categoryRepository.findByNameIgnoreCase(category1.getName().trim().toLowerCase())).thenReturn(Optional.of(category1));
        when(productRepository.findAllByCategory(category1, pageable)).thenReturn(productsMock);

        Page<ProductResponse> result = categoryService.getProductsByCategory(category1.getName(), pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(categoryRepository).findByNameIgnoreCase(category1.getName().trim().toLowerCase());
        verify(productRepository).findAllByCategory(category1, pageable);
    }

    @Test
    void deveLancarExcecaoAoBuscarProductsPorCategoryECategoryNoaExistir() {

        when(categoryRepository.findByNameIgnoreCase(category1.getName().trim().toLowerCase())).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> categoryService.getProductsByCategory(category1.getName(), pageable));

        assertTrue(notFound.getMessage().contains("name"));

        verify(categoryRepository).findByNameIgnoreCase(category1.getName().trim().toLowerCase());
    }
}