package com.fedstock.backend.auth.api.dto;

import com.fedstock.backend.auth.application.AuthResult;

public record AuthResponse(
    UserSummaryResponse user
) {
    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(UserSummaryResponse.from(result.user()));
    }
}
