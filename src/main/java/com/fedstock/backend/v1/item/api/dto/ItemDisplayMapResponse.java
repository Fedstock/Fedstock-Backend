package com.fedstock.backend.v1.item.api.dto;

import java.util.List;

public record ItemDisplayMapResponse(
    List<ItemDisplayResponse> items
) {
    public record ItemDisplayResponse(
        String itemId,
        String itemName,
        String category,
        String mappingSource
    ) {
    }
}
