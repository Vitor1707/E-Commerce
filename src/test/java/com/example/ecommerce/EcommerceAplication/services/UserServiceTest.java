package com.example.ecommerce.EcommerceAplication.services;

import com.example.ecommerce.EcommerceAplication.dtos.response.UserResponse;
import com.example.ecommerce.EcommerceAplication.dtos.update.UserUpdateRequest;
import com.example.ecommerce.EcommerceAplication.exceptions.ConflictException;
import com.example.ecommerce.EcommerceAplication.exceptions.ResourceNotFoundException;
import com.example.ecommerce.EcommerceAplication.model.Role;
import com.example.ecommerce.EcommerceAplication.model.User;
import com.example.ecommerce.EcommerceAplication.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user1 = new User();

    private User user2 = new User();

    private UserUpdateRequest updateRequest;

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        user1 = new User(1L, "User1", "user1@email.com", List.of(Role.USER, Role.ADMIN), "pass123", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>(), new ArrayList<>());
        user2 =  new User(1L, "User2", "user2@email.com", List.of(Role.USER), "pass123", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>(), new ArrayList<>());
        updateRequest = new UserUpdateRequest("User Atualizado", "user_atualizado@email.com", "password Atualizada");
        pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id"));
    }

    @Test
    void devePaginarUsersSeUsersExistirem() {
        Page<User> usersMock = new PageImpl<>(List.of(user1, user2));

        when(userRepository.findAll(pageable)).thenReturn(usersMock);

        Page<UserResponse> result = userService.usersPaginated(pageable);

        assertNotNull(result);
        assertEquals("User1", result.getContent().get(0).getUsername());
        assertEquals("user1@email.com", result.getContent().get(0).getEmail());
        assertEquals("User2", result.getContent().get(1).getUsername());
        assertEquals("user2@email.com", result.getContent().get(1).getEmail());

        verify(userRepository).findAll(pageable);
    }

    @Test
    void deveRetornarUmaPageVaziaSeUsersNaoExistirem() {
        Page<User> usersMock = new PageImpl<>(List.of());

        when(userRepository.findAll(pageable)).thenReturn(usersMock);

        Page<UserResponse> result = userService.usersPaginated(pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findAll(pageable);
    }

    @Test
    void deveRetornarUmUserAoBuscarUserPorIdEExistir() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        UserResponse result = userService.findUserById(1L);

        assertNotNull(result);
        assertEquals("User1", result.getUsername());
        assertEquals("user1@email.com", result.getEmail());

        verify(userRepository).findById(1L);
    }

    @Test
    void deveLancarUmaExcecaoAoBuscarUserPorIdENaoExistir() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> userService.findUserById(999L));

        assertTrue(notFound.getMessage().contains("ID"));

        verify(userRepository).findById(999L);
    }

    @Test
    void deveAtualizarUserCasoDadosForemValidos() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.existsByEmailAndIdNot(updateRequest.getEmail(), 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(new User(1L, "User Atualizado", "user_atualizado@email.com", List.of(Role.USER), "password Atualizada", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>(), new ArrayList<>()));

        UserResponse result = userService.updateUser(1L, updateRequest);

        assertNotNull(result);
        assertEquals("User Atualizado", result.getUsername());
        assertEquals("user_atualizado@email.com", result.getEmail());
        assertEquals("password Atualizada", result.getPassword());

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmailAndIdNot(updateRequest.getEmail(), 1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarUserEEmailJaEstarEmUso() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.existsByEmailAndIdNot(updateRequest.getEmail(), 1L)).thenReturn(true);

        ConflictException conflict = assertThrows(ConflictException.class, () -> userService.updateUser(1L, updateRequest));

        assertTrue(conflict.getMessage().contains("email"));

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmailAndIdNot(updateRequest.getEmail(), 1L);
    }

    @Test
    void devePromoverUserParaAdminSeDadosForemValidos() {
        user1.setRoles(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenReturn(new User(1L, "User1", "user1@email.com", List.of(Role.ADMIN), "pass123", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>(), new ArrayList<>()));

        UserResponse result = userService.promoteToAdmin(1L);

        assertNotNull(result);
        assertEquals(Role.ADMIN, result.getRoles().get(0));

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deveLancarExcecaoAoPromoverUserParaAdminEEleJaForAdmin() {
        user1.setRoles(new ArrayList<>());
        user1.getRoles().add(Role.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        ConflictException conflict = assertThrows(ConflictException.class, () -> userService.promoteToAdmin(1L));

        assertTrue(conflict.getMessage().contains("já"));

        verify(userRepository).findById(1L);
    }

    @Test
    void deveRemoverUserDeAdminCasoDadosForemValidos() {
        user1.setRoles(new ArrayList<>());
        user1.getRoles().add(Role.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenReturn(new User(1L, "User1", "user1@email.com", new ArrayList<>(), "pass123", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>(), new ArrayList<>()));

        UserResponse result = userService.removeFromAdmin(1L);

        assertNotNull(result);
        assertTrue(result.getRoles().isEmpty());

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deveLancarUmaExcecaoAoRemoverUserDoAdminEEleNaoForAdmin() {
        user1.setRoles(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        ConflictException conflict = assertThrows(ConflictException.class, () -> userService.removeFromAdmin(1L));

        assertTrue(conflict.getMessage().contains("não"));

        verify(userRepository).findById(1L);
    }

    @Test
    void deveRemoverUmUserCasoDadosForemValidos() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.removeUser(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deveLancarExcecaoAoRemoverUserEIdNaoExistir() {
        when(userRepository.existsById(999L)).thenReturn(false);

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class, () -> userService.removeUser(999L));

        assertTrue(notFound.getMessage().contains("ID"));

        verify(userRepository).existsById(999L);
    }
}