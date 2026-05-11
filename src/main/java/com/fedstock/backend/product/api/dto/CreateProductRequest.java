package com.fedstock.backend.product.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
    @NotBlank
    @Size(max = 100)
    String name,

    @Size(max = 100)
    String category,

    @Size(max = 30)
    String unit,

    @JsonAlias("safety_stock")
    @PositiveOrZero
    Integer safetyStock,

    @PositiveOrZero
    Integer quantity
) {
}
