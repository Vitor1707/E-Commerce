package com.example.ecommerce.EcommerceAplication.repositories;

import com.example.ecommerce.EcommerceAplication.model.Category;
import com.example.ecommerce.EcommerceAplication.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAllByCategory(Category category, Pageable pageable);

    List<Product> findAllByCategory(Category category);

    Optional<Product> findByNameIgnoreCase(String name);

    Optional<Product> findByCategory(Category category);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}