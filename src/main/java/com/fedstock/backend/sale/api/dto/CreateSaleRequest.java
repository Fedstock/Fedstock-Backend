package com.fedstock.backend.sale.api.dto;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateSaleRequest(
    @JsonAlias("product_id")
    @NotNull
    Long productId,

    @JsonAlias("sold_quantity")
    @NotNull
    @Positive
    Integer soldQuantity,

    @JsonAlias("sold_at")
    OffsetDateTime soldAt
) {
}
