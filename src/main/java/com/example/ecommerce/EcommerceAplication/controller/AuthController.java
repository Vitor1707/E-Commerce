package com.example.ecommerce.EcommerceAplication.controller;

import com.example.ecommerce.EcommerceAplication.dtos.requests.LoginRequest;
import com.example.ecommerce.EcommerceAplication.dtos.requests.UserRequest;
import com.example.ecommerce.EcommerceAplication.dtos.responses.UserResponse;
import com.example.ecommerce.EcommerceAplication.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid UserRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid LoginRequest request) {
        String response = authService.login(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}