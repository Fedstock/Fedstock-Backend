package com.fedstock.backend.auth.api;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fedstock.backend.auth.api.dto.AuthUserResponse;
import com.fedstock.backend.auth.api.dto.JwtAuthResponse;
import com.fedstock.backend.auth.api.dto.LoginRequest;
import com.fedstock.backend.auth.api.dto.RegisterRequest;
import com.fedstock.backend.auth.api.dto.SignupResponse;
import com.fedstock.backend.auth.application.AuthResult;
import com.fedstock.backend.auth.application.AuthService;
import com.fedstock.backend.auth.security.JwtTokenProvider;
import com.fedstock.backend.main.error.BadRequestException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(
        AuthService authService,
        AuthenticationManager authenticationManager,
        JwtTokenProvider jwtTokenProvider
    ) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/signup")
    public SignupResponse signup(@Valid @RequestBody RegisterRequest request) {
        log.info("auth signup requested email={} storeId={} username={}", request.email(), request.storeId(), request.username());
        AuthResult result = authService.register(
            request.email(),
            request.username(),
            request.storeId(),
            request.password(),
            request.name()
        );
        String accessToken = authenticate(request.email(), request.password()).accessToken();
        log.info("auth signup succeeded email={} storeId={}", request.email(), request.storeId());
        return new SignupResponse(accessToken, AuthUserResponse.from(result.user()));
    }

    @PostMapping("/login")
    public JwtAuthResponse login(@Valid @RequestBody LoginRequest request) {
        String identifier = firstNonBlank(request.email(), request.storeId(), request.username());
        log.info("auth login requested identifier={}", identifier);
        return authenticate(identifier, request.password());
    }

    private JwtAuthResponse authenticate(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("email, storeId, or username is required.");
        }
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password)
        );
        JwtAuthResponse response = new JwtAuthResponse(jwtTokenProvider.generateAccessToken(authentication));
        log.info("auth login succeeded identifier={}", email);
        return response;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
