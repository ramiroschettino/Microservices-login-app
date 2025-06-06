package com.microservices.auth_service.dto;

public class RegisterRequest {
    private String email;
    private String password;
    private String role;

    public RegisterRequest() {
    }

    public RegisterRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String getRole() { return role; }

    public void setRole(String role) {
        this.role = role;
    }
}
