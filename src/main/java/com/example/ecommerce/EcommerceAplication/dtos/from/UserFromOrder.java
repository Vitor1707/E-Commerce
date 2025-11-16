package com.example.ecommerce.EcommerceAplication.dtos.from;

import com.example.ecommerce.EcommerceAplication.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserFromOrder {
    private Long id;
    private String username;

    public UserFromOrder(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
    }
}