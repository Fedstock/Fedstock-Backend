package com.fedstock.backend.v1.forecast.infrastructure;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.stereotype.Component;

@Component
public class ForecastResultStore {

    private final ConcurrentMap<String, JsonNode> results = new ConcurrentHashMap<>();

    public void save(String analysisId, JsonNode result) {
        results.put(analysisId, result);
    }

    public Optional<JsonNode> find(String analysisId) {
        return Optional.ofNullable(results.get(analysisId));
    }
}
