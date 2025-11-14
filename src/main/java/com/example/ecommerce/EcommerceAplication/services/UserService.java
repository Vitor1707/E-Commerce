package com.example.ecommerce.EcommerceAplication.services;

import com.example.ecommerce.EcommerceAplication.dtos.requests.UserUpdateRequest;
import com.example.ecommerce.EcommerceAplication.dtos.responses.UserResponse;
import com.example.ecommerce.EcommerceAplication.exceptions.ConflictException;
import com.example.ecommerce.EcommerceAplication.exceptions.ResourceNotFoundException;
import com.example.ecommerce.EcommerceAplication.model.User;
import com.example.ecommerce.EcommerceAplication.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<UserResponse> UsersPaginated(Pageable pageable) {
        Page<User> usersPage = userRepository.findAll(pageable);
        return usersPage.map(UserResponse::new);
    }

    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        return updateByField(user, request);
    }

    public void removeUser(Long id) {
        if(!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }

        userRepository.deleteById(id);
    }

    private UserResponse updateByField(User user, UserUpdateRequest request) {
        if(
                request.getUsername() != null &&
                !request.getUsername().equalsIgnoreCase(user.getUsername()) &&
                !request.getUsername().isEmpty()
        ) {

            user.setUsername(request.getUsername());
        }

        if(
                request.getEmail() != null &&
                        !request.getEmail().equalsIgnoreCase(user.getEmail()) &&
                        !request.getEmail().isEmpty()
        ) {

            if(userRepository.existsByEmailAndIdNot(request.getEmail(), user.getId())) {
                throw new ConflictException("email", request.getEmail());
            }

            user.setEmail(request.getEmail());
        }
        if(
                request.getPassword() != null &&
                        !request.getPassword().equalsIgnoreCase(user.getPassword()) &&
                        !request.getPassword().isEmpty()
        ) {

            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User userUpdated = userRepository.save(user);
        return new UserResponse(userUpdated);
    }
}