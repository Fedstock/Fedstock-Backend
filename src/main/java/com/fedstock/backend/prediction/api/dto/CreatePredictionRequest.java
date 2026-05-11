package com.fedstock.backend.prediction.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreatePredictionRequest(
    @JsonAlias("product_id")
    @NotNull
    Long productId,

    @JsonAlias("predicted_daily_sales")
    @NotNull
    @PositiveOrZero
    BigDecimal predictedDailySales,

    @JsonAlias("current_quantity")
    @NotNull
    @PositiveOrZero
    Integer currentQuantity,

    @JsonAlias("expected_stockout_date")
    LocalDate expectedStockoutDate,

    @JsonAlias("recommendation_quantity")
    @PositiveOrZero
    Integer recommendationQuantity
) {
}
