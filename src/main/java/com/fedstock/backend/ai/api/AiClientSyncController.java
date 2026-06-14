package com.fedstock.backend.ai.api;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fedstock.backend.ai.api.dto.ClusterAssignmentRequest;
import com.fedstock.backend.ai.application.AiClientSyncService;

@RestController
@RequestMapping("/api/ai/clients")
public class AiClientSyncController {

    private final AiClientSyncService aiClientSyncService;

    public AiClientSyncController(AiClientSyncService aiClientSyncService) {
        this.aiClientSyncService = aiClientSyncService;
    }

    @PostMapping("/cluster-assignment")
    public ResponseEntity<?> assignCluster(@Valid @RequestBody ClusterAssignmentRequest request) {
        return aiClientSyncService.assignCluster(request);
    }

    @PostMapping("/{clientId}/fl-model")
    public ResponseEntity<?> syncFlModel(
        @PathVariable("clientId") String clientId,
        @RequestParam("client_id") String bodyClientId,
        @RequestParam("scope") String scope,
        @RequestParam("round_id") String roundId,
        @RequestParam(name = "sample_weight", required = false) Integer sampleWeight,
        @RequestParam("model_file") MultipartFile modelFile
    ) {
        return aiClientSyncService.syncFlModel(clientId, bodyClientId, scope, roundId, sampleWeight, modelFile);
    }

    @GetMapping("/{clientId}/fl-model")
    public ResponseEntity<?> downloadFlModel(@PathVariable("clientId") String clientId) {
        return aiClientSyncService.downloadFlModel(clientId);
    }
}
