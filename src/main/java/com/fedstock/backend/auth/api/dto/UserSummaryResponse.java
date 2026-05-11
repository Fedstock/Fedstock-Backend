package com.fedstock.backend.auth.api.dto;

import com.fedstock.backend.auth.application.UserPrincipal;

public record UserSummaryResponse(
    Long id,
    String email,
    String name
) {
    public static UserSummaryResponse from(UserPrincipal user) {
        return new UserSummaryResponse(user.id(), user.email(), user.name());
    }
}
