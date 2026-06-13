package com.fedstock.backend.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank
    @Email
    String email,

    @NotBlank
    @Size(max = 100)
    String storeId,

    @NotBlank
    @Size(max = 255)
    String username,

    @NotBlank
    @Size(min = 8)
    String password,

    @NotBlank
    @Size(max = 100)
    String name
) {
}
