package com.fedstock.backend.v1.localai.application;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.stereotype.Service;

import com.fedstock.backend.v1.shared.ai.AiBackendClient;

@Service
public class LocalAiService {

    private final AiBackendClient aiBackendClient;

    public LocalAiService(AiBackendClient aiBackendClient) {
        this.aiBackendClient = aiBackendClient;
    }

    public JsonNode health() {
        return aiBackendClient.getHealth();
    }
}
