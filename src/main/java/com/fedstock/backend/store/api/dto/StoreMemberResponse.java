package com.fedstock.backend.store.api.dto;

import java.time.LocalDateTime;

import com.fedstock.backend.auth.infrastructure.UserEntity;
import com.fedstock.backend.store.infrastructure.StoreMemberEntity;

public record StoreMemberResponse(
    Long id,
    String role,
    LocalDateTime joinedAt,
    Long userId,
    String email,
    String name
) {
    public static StoreMemberResponse from(StoreMemberEntity member) {
        UserEntity user = member.getUser();
        return new StoreMemberResponse(
            member.getId(),
            member.getRole().name(),
            member.getCreatedAt(),
            user.getId(),
            user.getEmail(),
            user.getName()
        );
    }
}
