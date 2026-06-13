package com.fedstock.backend.admin.s3.api.dto;

import jakarta.validation.constraints.NotBlank;

public record S3AdminPasswordRequest(
    @NotBlank
    String pw
) {
}
