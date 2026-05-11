package com.fedstock.backend.store.api.dto;

import java.time.LocalDateTime;

import com.fedstock.backend.auth.infrastructure.UserEntity;
import com.fedstock.backend.store.infrastructure.StoreMemberEntity;

public record AddStoreMemberResponse(
    Long id,
    Long userId,
    Long storeId,
    String role,
    LocalDateTime joinedAt,
    String email,
    String name
) {
    public static AddStoreMemberResponse from(StoreMemberEntity member) {
        UserEntity user = member.getUser();
        return new AddStoreMemberResponse(
            member.getId(),
            user.getId(),
            member.getStore().getId(),
            member.getRole().name(),
            member.getCreatedAt(),
            user.getEmail(),
            user.getName()
        );
    }
}
