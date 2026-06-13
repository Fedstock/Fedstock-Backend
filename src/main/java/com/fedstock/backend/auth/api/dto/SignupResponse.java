package com.fedstock.backend.auth.api.dto;

public record SignupResponse(
    String accessToken,
    AuthUserResponse user
) {
}
