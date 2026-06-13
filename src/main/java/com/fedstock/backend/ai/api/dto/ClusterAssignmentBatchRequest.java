package com.fedstock.backend.ai.api.dto;

import java.util.List;

public record ClusterAssignmentBatchRequest(
    String scope,
    String roundId,
    Integer expectedClientCount,
    List<ClusterAssignmentBatchItem> clients
) {
}
