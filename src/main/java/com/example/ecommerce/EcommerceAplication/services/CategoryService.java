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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }


    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = new Category();

        if(categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("name", request.getName());
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category categorySaved = categoryRepository.save(category);

        return new CategoryResponse(categorySaved);
    }

    public Page<CategoryResponse> categoriesPaginated(Pageable pageable) {
        Page<Category> categoriesPage = categoryRepository.findAll(pageable);

        return categoriesPage.map(CategoryResponse::new);
    }

    public Page<ProductResponse> getProductsByCategory(String name, Pageable pageable) {
        Category category = categoryRepository.findByNameIgnoreCase(name.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "name", name));

        Page<Product> productsPage = productRepository.findAllByCategory(category, pageable);

        return productsPage.map(ProductResponse::new);
    }
}