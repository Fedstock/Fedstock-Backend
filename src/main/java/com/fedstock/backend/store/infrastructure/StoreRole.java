package com.fedstock.backend.store.infrastructure;

import com.fedstock.backend.main.error.BadRequestException;

public enum StoreRole {
    OWNER,
    STAFF;

    public static StoreRole fromNullable(String value, StoreRole defaultRole) {
        if (value == null || value.isBlank()) {
            return defaultRole;
        }

        try {
            return StoreRole.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Store role must be OWNER or STAFF.");
        }
    }
}
