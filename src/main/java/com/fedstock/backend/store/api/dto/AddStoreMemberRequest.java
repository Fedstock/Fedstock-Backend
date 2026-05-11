package com.fedstock.backend.store.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AddStoreMemberRequest(
    @NotBlank
    @Email
    String email,

    String role
) {
}
