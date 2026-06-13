package com.fedstock.backend.auth.api.dto;

import com.fedstock.backend.auth.application.UserPrincipal;
import com.fedstock.backend.auth.security.SecurityUserDetails;

public record AuthUserResponse(
    String id,
    String email,
    String storeId,
    String name
) {
    public static AuthUserResponse from(UserPrincipal user) {
        String id = user.id() == null ? null : user.id().toString();
        return new AuthUserResponse(id, user.email(), user.storeId(), user.name());
    }

    public static AuthUserResponse from(SecurityUserDetails user) {
        String id = user.id() == null ? null : user.id().toString();
        return new AuthUserResponse(id, user.email(), user.storeId(), user.name());
    }
}
