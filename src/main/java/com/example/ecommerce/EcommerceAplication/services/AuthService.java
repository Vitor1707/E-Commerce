package com.example.ecommerce.EcommerceAplication.services;

import com.example.ecommerce.EcommerceAplication.dtos.request.LoginRequest;
import com.example.ecommerce.EcommerceAplication.dtos.request.UserRequest;
import com.example.ecommerce.EcommerceAplication.dtos.response.UserResponse;
import com.example.ecommerce.EcommerceAplication.exceptions.ConflictException;
import com.example.ecommerce.EcommerceAplication.exceptions.ResourceNotFoundException;
import com.example.ecommerce.EcommerceAplication.model.Role;
import com.example.ecommerce.EcommerceAplication.model.User;
import com.example.ecommerce.EcommerceAplication.repositories.UserRepository;
import com.example.ecommerce.EcommerceAplication.security.JwtService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManage;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManage, JwtService jwtService) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManage = authenticationManage;
        this.jwtService = jwtService;
    }

    public UserResponse register(UserRequest request) {
        User user = modelMapper.map(request, User.class);

        if(userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("email", request.getEmail());
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.getRoles().add(Role.USER);

        User userSaved  = userRepository.save(user);
        return new UserResponse(userSaved);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        Authentication authentication = authenticationManage.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = jwtService.generateToken(user.getEmail());

        return token;
    }
}