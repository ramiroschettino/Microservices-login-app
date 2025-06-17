package com.microservices.user_service.dto;

public class UserResponse {
    private Long id;
    private String email;
    private String role;
    private String password;

    public UserResponse() {}

    public UserResponse(Long id, String email, String role, String password) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.password = password;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}