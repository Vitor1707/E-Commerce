package com.example.ecommerce.EcommerceAplication.repositories;

import com.example.ecommerce.EcommerceAplication.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
