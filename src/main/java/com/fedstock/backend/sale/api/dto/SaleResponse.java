package com.fedstock.backend.sale.api.dto;

import java.time.LocalDateTime;

import com.fedstock.backend.sale.infrastructure.SaleEntity;

public record SaleResponse(
    Long id,
    Long productId,
    String productName,
    Integer soldQuantity,
    LocalDateTime soldAt,
    LocalDateTime createdAt
) {
    public static SaleResponse from(SaleEntity sale) {
        return new SaleResponse(
            sale.getId(),
            sale.getProduct().getId(),
            sale.getProduct().getName(),
            sale.getSoldQuantity(),
            sale.getSoldAt(),
            sale.getCreatedAt()
        );
    }
}
