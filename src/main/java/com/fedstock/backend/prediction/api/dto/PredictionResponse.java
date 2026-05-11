package com.fedstock.backend.prediction.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fedstock.backend.prediction.infrastructure.InventoryPredictionEntity;

public record PredictionResponse(
    Long id,
    Long productId,
    String productName,
    BigDecimal predictedDailySales,
    Integer currentQuantity,
    LocalDate expectedStockoutDate,
    Integer recommendationQuantity,
    LocalDateTime predictedAt
) {
    public static PredictionResponse from(InventoryPredictionEntity prediction) {
        return new PredictionResponse(
            prediction.getId(),
            prediction.getProduct().getId(),
            prediction.getProduct().getName(),
            prediction.getPredictedDailySales(),
            prediction.getCurrentQuantity(),
            prediction.getExpectedStockoutDate(),
            prediction.getRecommendationQuantity(),
            prediction.getPredictedAt()
        );
    }
}
