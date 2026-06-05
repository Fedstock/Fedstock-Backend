package com.fedstock.backend.v1.auth.api.dto;

public final class V1AuthResponses {

    private V1AuthResponses() {
    }

    public record LoginResponse(
        StoreResponse store
    ) {
    }

    public record StoreResponse(
        String storeId,
        String storeName,
        String role
    ) {
    }

    public record LogoutResponse(
        boolean success
    ) {
    }
}
