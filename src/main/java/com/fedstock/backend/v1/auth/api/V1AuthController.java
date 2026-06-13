package com.fedstock.backend.v1.auth.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fedstock.backend.auth.api.dto.LogoutResponse;

@RestController
@RequestMapping("/api/auth")
public class V1AuthController {

    @PostMapping("/logout")
    public LogoutResponse logout() {
        return new LogoutResponse(true);
    }
}
