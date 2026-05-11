package com.fedstock.backend.store.api.dto;

import java.time.LocalDateTime;

import com.fedstock.backend.store.infrastructure.StoreEntity;
import com.fedstock.backend.store.infrastructure.StoreMemberEntity;

public record StoreResponse(
    Long id,
    String name,
    String businessType,
    String role,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static StoreResponse from(StoreMemberEntity member) {
        StoreEntity store = member.getStore();
        return new StoreResponse(
            store.getId(),
            store.getName(),
            store.getBusinessType(),
            member.getRole().name(),
            store.getCreatedAt(),
            store.getUpdatedAt()
        );
    }
}
