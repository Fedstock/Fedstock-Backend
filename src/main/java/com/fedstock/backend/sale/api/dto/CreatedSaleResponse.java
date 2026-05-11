package com.fedstock.backend.sale.api.dto;

import java.time.LocalDateTime;

import com.fedstock.backend.sale.infrastructure.SaleEntity;

public record CreatedSaleResponse(
    Long id,
    Long productId,
    Integer soldQuantity,
    LocalDateTime soldAt,
    LocalDateTime createdAt
) {
    public static CreatedSaleResponse from(SaleEntity sale) {
        return new CreatedSaleResponse(
            sale.getId(),
            sale.getProduct().getId(),
            sale.getSoldQuantity(),
            sale.getSoldAt(),
            sale.getCreatedAt()
        );
    }
}
