package com.example.ecommerce.EcommerceAplication.services;

import com.example.ecommerce.EcommerceAplication.dtos.requests.ProductRequest;
import com.example.ecommerce.EcommerceAplication.dtos.responses.CategoryResponse;
import com.example.ecommerce.EcommerceAplication.dtos.updates.ProductUpdateRequest;
import com.example.ecommerce.EcommerceAplication.dtos.responses.ProductResponse;
import com.example.ecommerce.EcommerceAplication.exceptions.ConflictException;
import com.example.ecommerce.EcommerceAplication.exceptions.ResourceNotFoundException;
import com.example.ecommerce.EcommerceAplication.model.Category;
import com.example.ecommerce.EcommerceAplication.model.Product;
import com.example.ecommerce.EcommerceAplication.repositories.CategoryRepository;
import com.example.ecommerce.EcommerceAplication.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    public Page<ProductResponse> productsPaginated(Pageable pageable) {
        Page<Product> productsPage = productRepository.findAll(pageable);
        return productsPage.map(ProductResponse::new);
    }

    public ProductResponse addProduct(ProductRequest request) {
        Category category = categoryRepository.findByNameIgnoreCase(request.getCategory().getName().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "name", request.getCategory().getName()));

        if(productRepository.existsByNameIgnoreCase(request.getName().trim().toLowerCase())) {
            throw new ConflictException("name", request.getName());
        }

        Product product = new Product();

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setCategory(category);
        product.setDescription(request.getDescription());
        product.setStockQuantity(request.getStockQuantity());

        Product productSaved = productRepository.save(product);
        return new ProductResponse(productSaved);
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
                && !request.getName().equalsIgnoreCase(product.getName())
                && !request.getName().isEmpty()
        ) {
            if(productRepository.existsByNameAndIdNot(request.getName(), product.getId())) {
                throw new ConflictException("name", request.getName());
            }

            product.setName(request.getName());
        }

        if(
                request.getPrice() != null
                && !request.getPrice().equals(product.getPrice())
        ) {

            product.setPrice(request.getPrice());
        }

        if(
                request.getDescription() != null
                        && !request.getDescription().equalsIgnoreCase(product.getDescription())
                        && !request.getDescription().isEmpty()
        ) {

            product.setDescription(request.getDescription());
        }

        if(
                request.getStockQuantity() != null
                        && !request.getStockQuantity().equals(product.getStockQuantity())
        ) {

            product.setStockQuantity(request.getStockQuantity());
        }

        Product productUpdated = productRepository.save(product);
        return new ProductResponse(productUpdated);
    }
}