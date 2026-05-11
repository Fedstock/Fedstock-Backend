package com.fedstock.backend.product.api.dto;

import java.time.LocalDateTime;

import com.fedstock.backend.product.infrastructure.InventoryEntity;

public record InventoryResponse(
    Long productId,
    Integer quantity,
    LocalDateTime updatedAt
) {
    public static InventoryResponse from(InventoryEntity inventory) {
        return new InventoryResponse(
            inventory.getProduct().getId(),
            inventory.getQuantity(),
            inventory.getUpdatedAt()
        );
    }
}
