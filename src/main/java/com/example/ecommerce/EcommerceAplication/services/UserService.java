package com.example.ecommerce.EcommerceAplication.services;

import com.example.ecommerce.EcommerceAplication.dtos.response.UserResponse;
import com.example.ecommerce.EcommerceAplication.dtos.update.UserUpdateRequest;
import com.example.ecommerce.EcommerceAplication.exceptions.ConflictException;
import com.example.ecommerce.EcommerceAplication.exceptions.ResourceNotFoundException;
import com.example.ecommerce.EcommerceAplication.model.Role;
import com.example.ecommerce.EcommerceAplication.model.User;
import com.example.ecommerce.EcommerceAplication.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<UserResponse> usersPaginated(Pageable pageable) {
        Page<User> usersPage = userRepository.findAll(pageable);

        return usersPage.map(UserResponse::new);
    }

    public UserResponse findUserById(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::new)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        return updateUserByField(user, request);
    }

    public UserResponse promoteToAdmin(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if(user.getRoles().contains(Role.ADMIN)) {
            throw new ConflictException("User já é ADMIN");
        }

        user.getRoles().add(Role.ADMIN);

        User userUpdated = userRepository.save(user);
        return new UserResponse(userUpdated);
    }

    public UserResponse removeFromAdmin(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if(!user.getRoles().contains(Role.ADMIN)) {
            throw new ConflictException("User não é ADMIN");
        }

        user.getRoles().remove(Role.ADMIN);

        User userUpdated = userRepository.save(user);
        return new UserResponse(userUpdated);
    }

    public void removeUser(Long id) {
        if(!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }

        userRepository.deleteById(id);
    }

    private UserResponse updateUserByField(User user, UserUpdateRequest request) {
        if(
                request.getUsername() != null
                && !user.getUsername().equalsIgnoreCase(request.getUsername())
                && !request.getUsername().isEmpty()
        ) {

            user.setUsername(request.getUsername());
        }

        if(
                request.getEmail() != null
                && !user.getEmail().equalsIgnoreCase(request.getEmail())
                && !request.getEmail().isEmpty()
        ) {

            if(userRepository.existsByEmailAndIdNot(request.getEmail(), user.getId())) {
                throw new ConflictException("email", request.getEmail());
            }

            user.setEmail(request.getEmail());
        }

        if(
                request.getPassword() != null
                        && !user.getPassword().equalsIgnoreCase(request.getPassword())
                        && !request.getPassword().isEmpty()
        ) {

            user.setPassword(request.getPassword());
        }

        User userUpdated = userRepository.save(user);
        return new UserResponse(userUpdated);
    }
}