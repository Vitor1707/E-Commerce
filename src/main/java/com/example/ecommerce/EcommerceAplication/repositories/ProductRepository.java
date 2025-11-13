package com.example.ecommerce.EcommerceAplication.repositories;

import com.example.ecommerce.EcommerceAplication.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
