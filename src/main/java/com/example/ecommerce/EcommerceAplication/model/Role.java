package com.example.ecommerce.EcommerceAplication.model;

public enum Role {
    USER, ADMIN;

    public String getAuthority() {
        return this.name();
    }
}