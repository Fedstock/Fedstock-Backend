package com.fedstock.backend.ai.api.dto;

public record QueuedAiResponse(
    String api,
    String scope,
    String roundId,
    String clientId,
    Integer expectedClientCount,
    Long receivedClientCount,
    String status,
    boolean forwarded
) {
}
