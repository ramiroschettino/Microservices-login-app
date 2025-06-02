package com.microservices.auth_service.service;

import com.microservices.auth_service.client.UserClient;
import com.microservices.auth_service.dto.*;
import com.microservices.auth_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserClient userClient;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        // 1. Crear usuario en user-service
        UserResponse userResponse = userClient.createUser(
                new RegisterRequest(
                        request.getEmail(),
                        passwordEncoder.encode(request.getPassword())
                )
        );

        // 2. Generar tokens
        UserDetails userDetails = User.builder()
                .username(userResponse.getEmail())
                .password("") // No necesario para el token
                .roles(userResponse.getRole())
                .build();

        return generateTokens(userDetails);
    }

    public AuthResponse login(LoginRequest request) {
        // 1. Autenticar credenciales
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. Obtener usuario de user-service
        UserResponse userResponse = userClient.getUserByEmail(request.getEmail());

        // 3. Generar tokens
        UserDetails userDetails = User.builder()
                .username(userResponse.getEmail())
                .password("")
                .roles(userResponse.getRole())
                .build();

        return generateTokens(userDetails);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String email = jwtService.extractUsername(request.getRefreshToken());

        // Verificar usuario en user-service
        UserResponse userResponse = userClient.getUserByEmail(email);

        UserDetails userDetails = User.builder()
                .username(userResponse.getEmail())
                .password("")
                .roles(userResponse.getRole())
                .build();

        if (!jwtService.isTokenValid(request.getRefreshToken(), userDetails)) {
            throw new RuntimeException("Refresh token inválido");
        }

        return new AuthResponse(
                jwtService.generateToken(userDetails),
                request.getRefreshToken()
        );
    }

    private AuthResponse generateTokens(UserDetails userDetails) {
        return new AuthResponse(
                jwtService.generateToken(userDetails),
                jwtService.generateRefreshToken(userDetails)
        );
    }
}