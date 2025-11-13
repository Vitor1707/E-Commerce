package com.example.ecommerce.EcommerceAplication.repositories;

import com.example.ecommerce.EcommerceAplication.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
