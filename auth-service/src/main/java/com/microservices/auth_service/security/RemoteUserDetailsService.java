package com.microservices.auth_service.security;

import com.microservices.auth_service.client.UserClient;
import com.microservices.auth_service.dto.UserResponse;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class RemoteUserDetailsService implements UserDetailsService {

    private final UserClient userClient;

    public RemoteUserDetailsService(UserClient userClient) {
        this.userClient = userClient;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserResponse user = userClient.getUserByEmail(email);
        if (user == null || user.getEmail() == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword()) // bcrypt hash desde user-service
                .roles(user.getRole()) // ej: "ADMIN", "USER"
                .build();
    }
}
