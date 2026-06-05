package com.fedstock.backend.v1.ai.api;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fedstock.backend.v1.ai.application.AiGatewayService;

@RestController
@RequestMapping("/api/v1/ai")
public class AiGatewayController {

    private final AiGatewayService aiGatewayService;

    public AiGatewayController(AiGatewayService aiGatewayService) {
        this.aiGatewayService = aiGatewayService;
    }

    @GetMapping("/health")
    public ResponseEntity<JsonNode> health() {
        return aiGatewayService.health();
    }

    @PostMapping("/analyze-csv")
    public ResponseEntity<JsonNode> analyzeCsv(
        @RequestParam MultipartFile file
    ) {
        return aiGatewayService.analyzeCsv(file);
    }

    @PostMapping("/clients/register")
    public ResponseEntity<JsonNode> registerClient(
        @RequestParam("client_id") String clientId,
        @RequestParam("model_file") MultipartFile modelFile,
        @RequestParam(name = "importance_file", required = false) MultipartFile importanceFile,
        @RequestParam(name = "importance_json", required = false) String importanceJson,
        @RequestParam(name = "sample_weight", required = false) Integer sampleWeight
    ) {
        return aiGatewayService.registerClient(clientId, modelFile, importanceFile, importanceJson, sampleWeight);
    }

    @GetMapping("/clients/{clientId}/fl-model")
    public ResponseEntity<byte[]> downloadFlModel(
        @PathVariable String clientId
    ) {
        return aiGatewayService.downloadFlModel(clientId);
    }
}
