package com.example.ecommerce.EcommerceAplication.services;

import com.example.ecommerce.EcommerceAplication.dtos.requests.CategoryRequest;
import com.example.ecommerce.EcommerceAplication.dtos.responses.CategoryResponse;
import com.example.ecommerce.EcommerceAplication.dtos.updates.CategoryUpdateRequest;
import com.example.ecommerce.EcommerceAplication.exceptions.ConflictException;
import com.example.ecommerce.EcommerceAplication.exceptions.ResourceNotFoundException;
import com.example.ecommerce.EcommerceAplication.model.Category;
import com.example.ecommerce.EcommerceAplication.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    public CategoryService(CategoryRepository categoryRepository, ModelMapper modelMapper) {
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::new)
                .toList();
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        String normalizedName = request.getName().trim().toLowerCase();

        if(categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ConflictException("name", request.getName());
        }

        Category category = new Category();

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category categorySaved = categoryRepository.save(category);
        return new CategoryResponse(categorySaved);
    }

    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        return updateCategoryByField(category, request);
    }

    public void removeCategory(Long id) {
        if(!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", id);
        }

        categoryRepository.deleteById(id);
    }

    private CategoryResponse updateCategoryByField(Category category, CategoryUpdateRequest request) {
        if(
                request.getName() != null
                && !request.getName().equalsIgnoreCase(category.getName())
                && !request.getName().isEmpty()
        ) {

            if(categoryRepository.existsByNameAndIdNot(request.getName(), category.getId())) {
                throw new ConflictException("name", request.getName());
            }

            category.setName(request.getName());
        }
        if(
                request.getDescription() != null
                        && !request.getDescription().equalsIgnoreCase(category.getDescription())
                        && !request.getDescription().isEmpty()
        ) {

            category.setDescription(request.getDescription());
        }

        Category categoryUpdated = categoryRepository.save(category);
        return new CategoryResponse(categoryUpdated);
    }
}