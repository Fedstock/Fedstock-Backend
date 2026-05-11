package com.fedstock.backend.store.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateStoreRequest(
    @NotBlank
    @Size(max = 100)
    String name,

    @Size(max = 100)
    String businessType
) {
}
