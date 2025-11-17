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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Page<ProductResponse> productsPaginated(Pageable pageable) {
        Page<Product> productsPage = productRepository.findAll(pageable);

        return productsPage.map(ProductResponse::new);
    }

    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();

        if(productRepository.existsByName(request.getName())) {
            throw new ConflictException("name", request.getName());
        }

        if(!categoryRepository.existsByNameIgnoreCase(request.getCategory().trim().toLowerCase())) {
            throw new ResourceNotFoundException("Category", "name", request.getCategory());
        }

        Category category = categoryRepository.findByNameIgnoreCase(request.getCategory().trim().toLowerCase())
                        .orElseThrow(() -> new ResourceNotFoundException("Category", "name", request.getCategory()));

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setDescription(request.getDescription());
        product.setCategory(category);

        Product productSaved = productRepository.save(product);

        return new ProductResponse(productSaved);
    }

    public ProductResponse findProductById(Long id) {
        return productRepository.findById(id)
                .map(ProductResponse::new)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    public ProductResponse findProductByName(String name) {
        return productRepository.findByNameIgnoreCase(name)
                .map(ProductResponse::new)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "name", name));
    }

    public Page<ProductResponse> findProductByCategory(String nameCategory, Pageable pageable) {
        Category category = categoryRepository.findByNameIgnoreCase(nameCategory)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "name", nameCategory));

        Page<Product> productsPage = productRepository.findAllByCategory(category, pageable);

        return productsPage.map(ProductResponse::new);
    }

    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        return updateProductByField(product, request);
    }

    public void removeProduct(Long id) {
        if(!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }

        productRepository.deleteById(id);
    }

    private ProductResponse updateProductByField(Product product, ProductUpdateRequest request) {
        if(
                request.getName() != null
                && !product.getName().equalsIgnoreCase(request.getName())
                && !request.getName().isEmpty()
        ) {

            if(productRepository.existsByNameAndIdNot(request.getName(), product.getId())) {
                throw new ConflictException("name", request.getName());
            }

            product.setName(request.getName());
        }

        if(
                request.getPrice() != null
                && !(product.getPrice().equals(request.getPrice()))
        ) {
            product.setPrice(request.getPrice());
        }

        if(
                request.getStockQuantity() != null
                && !(product.getStockQuantity().equals(request.getStockQuantity()))
                && request.getStockQuantity() > product.getStockQuantity()
        ) {
            product.setStockQuantity(request.getStockQuantity());
        }

        if(
                request.getDescription() != null
                && !product.getDescription().equalsIgnoreCase(request.getDescription())
                && !request.getDescription().isEmpty()
        ) {
            product.setDescription(request.getDescription());
        }

        if(
                request.getCategory() != null
                && !request.getCategory().isEmpty()
        ) {

            if(!categoryRepository.existsByNameIgnoreCase(request.getCategory().trim().toLowerCase())) {
                throw new ResourceNotFoundException("Category", "name", request.getCategory());
            }

            product.getCategory().setName(request.getCategory());
        }

        Product productUpdated = productRepository.save(product);
        return new ProductResponse(productUpdated);
    }
}