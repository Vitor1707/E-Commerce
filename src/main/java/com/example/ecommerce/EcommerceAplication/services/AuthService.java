package com.example.ecommerce.EcommerceAplication.services;

import com.example.ecommerce.EcommerceAplication.dtos.requests.LoginRequest;
import com.example.ecommerce.EcommerceAplication.dtos.requests.UserRequest;
import com.example.ecommerce.EcommerceAplication.dtos.responses.UserResponse;
import com.example.ecommerce.EcommerceAplication.exceptions.ConflictException;
import com.example.ecommerce.EcommerceAplication.exceptions.ResourceNotFoundException;
import com.example.ecommerce.EcommerceAplication.model.Role;
import com.example.ecommerce.EcommerceAplication.model.User;
import com.example.ecommerce.EcommerceAplication.repositories.UserRepository;
import com.example.ecommerce.EcommerceAplication.security.JwtService;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, ModelMapper modelMapper, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserResponse register(UserRequest request) {
        User user = modelMapper.map(request, User.class);

        if(userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("email", request.getEmail());
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.getRoles().add(Role.USER);

        User userSaved = userRepository.save(user);
        return new UserResponse(userSaved);
    }

    public String login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        String token = jwtService.generatedToken(user.getEmail());

        return token;
    }
}