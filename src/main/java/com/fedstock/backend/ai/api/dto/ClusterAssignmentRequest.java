package com.fedstock.backend.ai.api.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ClusterAssignmentRequest(
    @NotBlank
    String scope,

    @NotBlank
    String roundId,

    @NotBlank
    String clientId,

    @NotNull
    @Positive
    Integer sampleCount,

    @NotEmpty
    List<@NotBlank String> featureNames,

    @NotEmpty
    List<@NotNull @PositiveOrZero BigDecimal> featureImportance,

    @Positive
    Integer expectedClientCount
) {
}
