package com.fedstock.backend.v1.auth.api;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fedstock.backend.auth.application.AuthResult;
import com.fedstock.backend.auth.application.AuthService;
import com.fedstock.backend.auth.application.UserPrincipal;
import com.fedstock.backend.v1.auth.api.dto.V1AuthResponses.LoginResponse;
import com.fedstock.backend.v1.auth.api.dto.V1AuthResponses.LogoutResponse;
import com.fedstock.backend.v1.auth.api.dto.V1AuthResponses.StoreResponse;
import com.fedstock.backend.v1.auth.api.dto.V1LoginRequest;

@RestController
@RequestMapping("/api/v1/auth")
public class V1AuthController {

    private static final String DEFAULT_ROLE = "STORE_MANAGER";

    private final AuthService authService;

    public V1AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody V1LoginRequest request) {
        AuthResult result = authService.login(request.storeId(), request.password());
        return new LoginResponse(store(result.user()));
    }

    @GetMapping("/me")
    public StoreResponse me() {
        return new StoreResponse("guest", "Guest Store", DEFAULT_ROLE);
    }

    @PostMapping("/logout")
    public LogoutResponse logout() {
        return new LogoutResponse(true);
    }

    private StoreResponse store(UserPrincipal user) {
        return new StoreResponse(user.email(), user.name(), DEFAULT_ROLE);
    }
}
