package com.fedstock.backend.prediction.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fedstock.backend.prediction.infrastructure.InventoryPredictionEntity;

public record CreatedPredictionResponse(
    Long id,
    Long productId,
    BigDecimal predictedDailySales,
    Integer currentQuantity,
    LocalDate expectedStockoutDate,
    Integer recommendationQuantity,
    LocalDateTime predictedAt
) {
    public static CreatedPredictionResponse from(InventoryPredictionEntity prediction) {
        return new CreatedPredictionResponse(
            prediction.getId(),
            prediction.getProduct().getId(),
            prediction.getPredictedDailySales(),
            prediction.getCurrentQuantity(),
            prediction.getExpectedStockoutDate(),
            prediction.getRecommendationQuantity(),
            prediction.getPredictedAt()
        );
    }
}
