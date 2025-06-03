package com.microservices.auth_service.service;

import com.microservices.auth_service.client.UserClient;
import com.microservices.auth_service.dto.*;
import com.microservices.auth_service.security.JwtService;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserClient userClient;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthService(UserClient userClient, PasswordEncoder passwordEncoder, AuthenticationManager authManager, JwtService jwtService) {
        this.userClient = userClient;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        // 1. Crear usuario en user-service
        UserResponse userResponse = userClient.createUser(
                new RegisterRequest(
                        request.getEmail(),
                        passwordEncoder.encode(request.getPassword())
                )
        );

        // 2. Generar tokens
        UserDetails userDetails = buildUserDetails(userResponse);
        return generateTokens(userDetails);
    }

    public AuthResponse login(LoginRequest request) {
        // 1. Autenticar credenciales
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Obtener usuario de user-service
        UserResponse userResponse = userClient.getUserByEmail(request.getEmail());

        // 3. Generar tokens
        UserDetails userDetails = buildUserDetails(userResponse);
        return generateTokens(userDetails);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        // 1. Extraer email del token
        String email = jwtService.extractEmail(request.getRefreshToken());

        // 2. Verificar usuario en user-service
        UserResponse userResponse = userClient.getUserByEmail(email);
        if (userResponse == null) {
            throw new RuntimeException("Usuario no encontrado para el refresh token");
        }

        // 3. Construir userDetails
        UserDetails userDetails = buildUserDetails(userResponse);

        // 4. Extraer rol
        String role = userDetails.getAuthorities().stream().findFirst().map(Object::toString).orElse("");

        // 5. Validar token
        if (!jwtService.isTokenValid(request.getRefreshToken(), userDetails.getUsername())) {
            throw new BadCredentialsException("Refresh token inválido");
        }

        return new AuthResponse(
                jwtService.generateToken(userDetails.getUsername(), role),
                request.getRefreshToken()
        );
    }

    private AuthResponse generateTokens(UserDetails userDetails) {
        String role = userDetails.getAuthorities().stream().findFirst().map(Object::toString).orElse("");
        return new AuthResponse(
                jwtService.generateToken(userDetails.getUsername(), role),
                jwtService.generateRefreshToken(userDetails.getUsername(), role)
        );
    }

    private UserDetails buildUserDetails(UserResponse userResponse) {
        return User.builder()
                .username(userResponse.getEmail())
                .password("") // No se usa aquí
                .roles(userResponse.getRole())
                .build();
    }
}
