package com.microservices.auth_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long expiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshExpiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email) {
        return generateToken(new HashMap<>(), email, expiration);
    }

    public String generateToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return generateToken(claims, email, expiration);
    }

    public String generateRefreshToken(String email) {
        return generateToken(new HashMap<>(), email, refreshExpiration);
    }

    public String generateRefreshToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return generateToken(claims, email, refreshExpiration);
    }

    private String generateToken(Map<String, Object> extraClaims, String subject, long tokenValidity) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + tokenValidity * 1000)) // segundos a ms
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, String email) {
        final String extractedEmail = extractEmail(token);
        return (extractedEmail != null && extractedEmail.equals(email)) && !isTokenExpired(token);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String extractedEmail = extractEmail(token);
        return (extractedEmail != null && extractedEmail.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public String extractEmail(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public String extractRole(String token) {
        try {
            return extractClaim(token, claims -> (String) claims.get("role"));
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration == null || expiration.before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            throw e;
        }
    }
}
