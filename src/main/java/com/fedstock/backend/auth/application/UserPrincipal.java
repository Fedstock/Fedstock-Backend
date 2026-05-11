package com.fedstock.backend.auth.application;

public record UserPrincipal(
    Long id,
    String email,
    String name
) {
}
