package com.fedstock.backend.v1.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

public record V1LoginRequest(
    @NotBlank
    String storeId,

    @NotBlank
    String password
) {
}
