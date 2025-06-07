package com.microservices.auth_service.client;

import com.microservices.auth_service.dto.LoginRequest;
import com.microservices.auth_service.dto.RegisterRequest;
import com.microservices.auth_service.dto.UserResponse;
import com.microservices.auth_service.security.FeignClientInterceptorConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "user-service",
        url = "${user-service-url}",
        configuration = FeignClientInterceptorConfig.class
)
public interface UserClient {
    @PostMapping("/api/users/validate")
    UserResponse validateUser(@RequestBody LoginRequest request);

    @PostMapping("/api/users")
    UserResponse createUser(@RequestBody RegisterRequest request);

    @GetMapping("/api/users/{email}")
    UserResponse getUserByEmail(@PathVariable String email);
}
