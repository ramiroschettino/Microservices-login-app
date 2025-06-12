package com.microservices.user_service.service;

import com.microservices.user_service.dto.LoginRequest;
import com.microservices.user_service.dto.RegisterRequest;
import com.microservices.user_service.dto.UserResponse;
import com.microservices.user_service.model.User;
import com.microservices.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El correo electrónico ya existe");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER"); // Forzar siempre USER
        userRepository.save(user);
        return new UserResponse(user.getId(), user.getEmail(), user.getRole());
    }

    public UserResponse validateUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }
        return new UserResponse(user.getId(), user.getEmail(), user.getRole());
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return new UserResponse(user.getId(), user.getEmail(), user.getRole());
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponse(user.getId(), user.getEmail(), user.getRole()))
                .collect(Collectors.toList());
    }
}