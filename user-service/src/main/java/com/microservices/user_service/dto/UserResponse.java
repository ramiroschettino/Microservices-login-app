package com.microservices.user_service.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserResponse {
    // Getters y setters
    private String email;
    private String role;

    public UserResponse() {}

    public UserResponse(String email, String role) {
        this.email = email;
        this.role = role;
    }

}
