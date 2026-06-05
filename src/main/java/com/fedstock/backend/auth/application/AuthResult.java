package com.fedstock.backend.auth.application;

public record AuthResult(
    UserPrincipal user
) {
}
