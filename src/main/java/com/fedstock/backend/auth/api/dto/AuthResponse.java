package com.fedstock.backend.auth.api.dto;

import com.fedstock.backend.auth.application.AuthResult;

public record AuthResponse(
    String token,
    UserSummaryResponse user
) {
    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(result.token(), UserSummaryResponse.from(result.user()));
    }
}
