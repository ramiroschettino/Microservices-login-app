package com.microservices.user_service.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterRequest {
    // Getters y setters
    private String email;
    private String password;

}
