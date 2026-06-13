package com.fedstock.backend.ai.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record ClusterAssignmentBatchItem(
    String clientId,
    Integer sampleCount,
    List<String> featureNames,
    List<BigDecimal> featureImportance
) {
}
