package com.fedstock.backend.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    String email,

    String storeId,

    String username,

    @NotBlank
    String password
) {
}
