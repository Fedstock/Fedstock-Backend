package com.fedstock.backend.auth.api;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fedstock.backend.auth.api.dto.AuthResponse;
import com.fedstock.backend.auth.api.dto.LoginRequest;
import com.fedstock.backend.auth.api.dto.RegisterRequest;
import com.fedstock.backend.auth.api.dto.UserSummaryResponse;
import com.fedstock.backend.auth.application.AuthService;
import com.fedstock.backend.auth.application.UserPrincipal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = AuthResponse.from(authService.register(
            request.email(),
            request.password(),
            request.name()
        ));
        URI location = ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path("/api/auth/me")
            .build()
            .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return AuthResponse.from(authService.login(request.email(), request.password()));
    }

    @GetMapping("/me")
    public UserSummaryResponse me(@CurrentUser UserPrincipal user) {
        return UserSummaryResponse.from(user);
    }
}
