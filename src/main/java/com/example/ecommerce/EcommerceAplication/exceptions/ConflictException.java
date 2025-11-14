package com.example.ecommerce.EcommerceAplication.exceptions;

public class ConflictException extends RuntimeException {

    public ConflictException(String fieldName, String fieldValue) {super(fieldName + " '" + fieldValue + "' já está em uso");}

    public ConflictException(String message) {
        super(message);
    }
}
