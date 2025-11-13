package com.example.ecommerce.EcommerceAplication.repositories;

import com.example.ecommerce.EcommerceAplication.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}