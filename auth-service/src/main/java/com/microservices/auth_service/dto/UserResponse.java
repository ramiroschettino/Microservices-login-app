package com.microservices.auth_service.dto;

public class UserResponse {
    private String email;
    private String role;
    private String password;

    public UserResponse() {
    }

    public UserResponse(String email, String role, String password) {
        this.email = email;
        this.role = role;
        this.password = password;
    }


    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
