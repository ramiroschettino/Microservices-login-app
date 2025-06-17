package com.microservices.auth_service.service;

import com.microservices.auth_service.client.UserClient;
import com.microservices.auth_service.dto.*;
import com.microservices.auth_service.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserClient userClient;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public AuthService(UserClient userClient, AuthenticationManager authManager, JwtService jwtService, UserDetailsService userDetailsService) {
        this.userClient = userClient;
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    public AuthResponse register(RegisterRequest request) {
        logger.info("Registrando usuario: {}", request.getEmail());
        UserResponse userResponse;
        try {
            userResponse = userClient.createUser(request);
            logger.info("Usuario registrado exitosamente: {}", userResponse.getEmail());
        } catch (Exception e) {
            logger.error("Error registrando usuario: {}", request.getEmail(), e);
            throw e;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(userResponse.getEmail());
        return generateTokens(userDetails);
    }

    public AuthResponse login(LoginRequest request) {
        logger.info("Intentando autenticar usuario: {}", request.getEmail());
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            logger.info("Autenticación exitosa para: {}", request.getEmail());
        } catch (InternalAuthenticationServiceException e) {
            // Analiza el mensaje de la excepción para distinguir el motivo
            if (e.getCause() != null && e.getCause().getMessage() != null) {
                String msg = e.getCause().getMessage();
                if (msg.contains("Usuario no encontrado")) {
                    throw new UsernameNotFoundException("Usuario no encontrado");
                }
                if (msg.contains("Credenciales inválidas")) {
                    throw new BadCredentialsException("Credenciales inválidas");
                }
            }
            throw new BadCredentialsException("Credenciales inválidas o usuario no encontrado");
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Credenciales inválidas");
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        } catch (Exception e) {
            logger.error("Error autenticando usuario: {}", request.getEmail(), e);
            throw new BadCredentialsException("Credenciales inválidas o usuario no encontrado");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        return generateTokens(userDetails);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        logger.info("Intentando refrescar token");
        String email = jwtService.extractEmail(request.getRefreshToken());
        logger.info("Email extraído del refresh token: {}", email);

        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(email);
            logger.info("Usuario obtenido de user-service para refresh: {}", userDetails.getUsername());
        } catch (Exception e) {
            logger.error("Usuario no encontrado para el refresh token: {}", email, e);
            throw new RuntimeException("Usuario no encontrado para el refresh token");
        }

        String role = userDetails.getAuthorities().stream().findFirst().map(Object::toString).orElse("");

        if (!jwtService.isTokenValid(request.getRefreshToken(), userDetails.getUsername())) {
            logger.warn("Refresh token inválido para usuario: {}", userDetails.getUsername());
            throw new BadCredentialsException("Refresh token inválido");
        }

        logger.info("Refresh token válido, generando nuevo access token para: {}", userDetails.getUsername());
        return new AuthResponse(
                jwtService.generateToken(userDetails.getUsername(), role),
                request.getRefreshToken()
        );
    }

    private AuthResponse generateTokens(UserDetails userDetails) {
        String role = userDetails.getAuthorities().stream().findFirst().map(Object::toString).orElse("");
        logger.info("Generando tokens para usuario: {} con rol: {}", userDetails.getUsername(), role);
        return new AuthResponse(
                jwtService.generateToken(userDetails.getUsername(), role),
                jwtService.generateRefreshToken(userDetails.getUsername(), role)
        );
    }
}