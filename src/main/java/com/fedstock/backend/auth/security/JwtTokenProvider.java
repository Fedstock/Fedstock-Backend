package com.fedstock.backend.auth.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpirationMs;

    public JwtTokenProvider(JwtProperties properties) {
        this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = properties.accessTokenExpirationMs();
    }

    public String generateAccessToken(Authentication authentication) {
        SecurityUserDetails user = (SecurityUserDetails) authentication.getPrincipal();
        Instant now = Instant.now();

        return Jwts.builder()
            .subject(user.getUsername())
            .claim("userId", user.id())
            .claim("storeId", user.storeId())
            .claim("name", user.name())
            .claim("role", user.role())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(accessTokenExpirationMs)))
            .signWith(secretKey)
            .compact();
    }

    public String getUsername(String token) {
        return claims(token).getSubject();
    }

    public boolean isValid(String token) {
        claims(token);
        return true;
    }

    private Claims claims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
