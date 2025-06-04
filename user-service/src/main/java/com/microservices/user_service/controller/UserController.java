package com.microservices.user_service.controller;

import com.microservices.user_service.dto.LoginRequest;
import com.microservices.user_service.dto.RegisterRequest;
import com.microservices.user_service.dto.UserResponse;
import com.microservices.user_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public UserResponse register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/validate")
    public UserResponse validateUser(@RequestBody LoginRequest request) {
        return userService.validateUser(request);
    }

    @GetMapping("/{email}")
    public UserResponse getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }
}
