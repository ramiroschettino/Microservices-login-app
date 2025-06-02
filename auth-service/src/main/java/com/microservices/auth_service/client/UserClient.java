package com.microservices.auth_service.client;

import com.microservices.auth_service.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "user-service",       // Nombre del servicio (debe coincidir con el application.yml de user-service)
        url = "${user-service.url}"  // URL base (ej: http://user-service:8081)
)
public interface UserClient {

    // Endpoint para validar credenciales (login)
    @PostMapping("/api/users/validate")
    UserResponse validateUser(@RequestBody LoginRequest request);

    // Endpoint para registrar usuario
    @PostMapping("/api/users")
    UserResponse createUser(@RequestBody RegisterRequest request);

    // Endpoint para obtener usuario por email
    @GetMapping("/api/users/{email}")
    UserResponse getUserByEmail(@PathVariable String email);
}