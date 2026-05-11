package com.fedstock.backend.product.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.PositiveOrZero;

public record InventoryUpsertRequest(
    @JsonAlias("qty")
    @PositiveOrZero
    Integer quantity
) {
}
