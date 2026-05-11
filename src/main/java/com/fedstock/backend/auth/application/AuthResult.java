package com.fedstock.backend.auth.application;

public record AuthResult(
    String token,
    UserPrincipal user
) {
}
