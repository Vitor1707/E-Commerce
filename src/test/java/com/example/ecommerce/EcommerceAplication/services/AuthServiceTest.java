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
import org.hibernate.validator.constraints.ModCheck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    User user = new User();

    UserRequest request = new UserRequest();

    LoginRequest loginRequest = new LoginRequest();

    @BeforeEach
    void setUp() {
        user = new User(1L, "User", "user@email.com", List.of(Role.USER), "pass123", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>(), new ArrayList<>());
        request = new UserRequest("Novo User", "novo_user@email.com", "password123");
        loginRequest = new LoginRequest("user@email.com", "pass123");
    }

    @Test
    void deveSalvarUserSeTodosOsDadosForemValidos() {
        when(modelMapper.map(request, User.class)).thenReturn(new User(null, "Novo User", "novo_user@email.com", new ArrayList<>(), "password123", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>(), new ArrayList<>()));
        when(userRepository.existsByEmail("novo_user@email.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("passEncode");
        when(userRepository.save(any(User.class))).thenReturn(new User(1L, "Novo User", "novo_user@email.com", List.of(Role.USER), "passEncode", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>(), new ArrayList<>()));

        UserResponse result = authService.register(request);

        assertNotNull(result);
        assertEquals("Novo User", result.getUsername());
        assertEquals("novo_user@email.com", result.getEmail());
        assertEquals("passEncode", result.getPassword());
        assertEquals(Role.USER, result.getRoles().get(0));

        verify(modelMapper).map(request, User.class);
        verify(userRepository).existsByEmail("novo_user@email.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deveLancarExcecaoAoRegistrarUserESeuEmailJaEstarEmUso() {
        when(modelMapper.map(request, User.class)).thenReturn(new User(null, "Novo User", "novo_user@email.com", new ArrayList<>(), "password123", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>(), new ArrayList<>()));
        when(userRepository.existsByEmail("novo_user@email.com")).thenReturn(true);

        ConflictException conflict = assertThrows(ConflictException.class, () -> authService.register(request));

        assertTrue(conflict.getMessage().contains("email"));

        verify(modelMapper).map(request, User.class);
        verify(userRepository).existsByEmail("novo_user@email.com");
    }

    @Test
    void deveCriarUmTokenAoRealizarLoginBemSucedido() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateToken(loginRequest.getEmail())).thenReturn("token.jwt.generate");

        String result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals("token.jwt.generate", result);

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(loginRequest.getEmail());
    }

    @Test
    void deveLancarExcecaoAoRealizarLoginEUserNaoForEncontrado() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> authService.login(loginRequest));

        assertTrue(notFound.getMessage().contains("email"));

        verify(userRepository).findByEmail(loginRequest.getEmail());
    }
}