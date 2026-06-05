package com.fedstock.backend.v1.ai.application;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fedstock.backend.main.error.BadRequestException;
import com.fedstock.backend.v1.shared.ai.AiBackendClient;

@Service
public class AiGatewayService {

    private final AiBackendClient aiBackendClient;

    public AiGatewayService(AiBackendClient aiBackendClient) {
        this.aiBackendClient = aiBackendClient;
    }

    public ResponseEntity<JsonNode> health() {
        return aiBackendClient.forwardHealth();
    }

    public ResponseEntity<JsonNode> analyzeCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("CSV file is required.");
        }
        return aiBackendClient.forwardAnalyzeCsv(file);
    }

    public ResponseEntity<JsonNode> registerClient(
        String clientId,
        MultipartFile modelFile,
        MultipartFile importanceFile,
        String importanceJson,
        Integer sampleWeight
    ) {
        if (clientId == null || clientId.isBlank()) {
            throw new BadRequestException("client_id is required.");
        }
        if (modelFile == null || modelFile.isEmpty()) {
            throw new BadRequestException("model_file is required.");
        }
        boolean hasImportanceFile = importanceFile != null && !importanceFile.isEmpty();
        boolean hasImportanceJson = importanceJson != null && !importanceJson.isBlank();
        if (!hasImportanceFile && !hasImportanceJson) {
            throw new BadRequestException("importance_file or importance_json is required.");
        }

        return aiBackendClient.forwardRegisterClient(
            clientId,
            modelFile,
            hasImportanceFile ? importanceFile : null,
            hasImportanceJson ? importanceJson : null,
            sampleWeight
        );
    }

    public ResponseEntity<byte[]> downloadFlModel(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw new BadRequestException("clientId is required.");
        }
        return aiBackendClient.forwardFlModel(clientId);
    }
}
