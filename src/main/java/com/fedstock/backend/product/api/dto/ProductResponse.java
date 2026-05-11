package com.fedstock.backend.product.api.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fedstock.backend.product.infrastructure.InventoryEntity;
import com.fedstock.backend.product.infrastructure.ProductEntity;

public record ProductResponse(
    Long id,
    Long storeId,
    String name,
    String category,
    String unit,
    Integer safetyStock,
    @JsonProperty("isActive")
    Boolean active,
    Integer quantity,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ProductResponse from(ProductEntity product) {
        InventoryEntity inventory = product.getInventory();
        return new ProductResponse(
            product.getId(),
            product.getStore().getId(),
            product.getName(),
            product.getCategory(),
            product.getUnit(),
            product.getSafetyStock(),
            product.getActive(),
            inventory == null ? null : inventory.getQuantity(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }
}
