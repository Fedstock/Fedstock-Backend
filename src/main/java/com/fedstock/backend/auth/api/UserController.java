package com.fedstock.backend.auth.api;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fedstock.backend.auth.api.dto.AuthUserResponse;
import com.fedstock.backend.auth.security.SecurityUserDetails;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public AuthUserResponse me(Authentication authentication) {
        return AuthUserResponse.from((SecurityUserDetails) authentication.getPrincipal());
    }
}
