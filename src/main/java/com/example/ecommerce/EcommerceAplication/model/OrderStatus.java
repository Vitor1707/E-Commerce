package com.example.ecommerce.EcommerceAplication.model;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public String getAuthority() {
        return this.name();
    }
}
