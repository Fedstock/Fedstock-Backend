package com.fedstock.backend.ai.application;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fedstock.backend.ai.api.dto.ClusterAssignmentBatchItem;
import com.fedstock.backend.ai.api.dto.ClusterAssignmentBatchRequest;
import com.fedstock.backend.ai.api.dto.ClusterAssignmentRequest;
import com.fedstock.backend.ai.api.dto.QueuedAiResponse;
import com.fedstock.backend.ai.infrastructure.AiBackendClient;
import com.fedstock.backend.ai.infrastructure.AiClusterAssignmentQueueEntity;
import com.fedstock.backend.ai.infrastructure.AiClusterAssignmentQueueJpaRepository;
import com.fedstock.backend.ai.infrastructure.AiFlModelQueueEntity;
import com.fedstock.backend.ai.infrastructure.AiFlModelQueueJpaRepository;
import com.fedstock.backend.ai.infrastructure.AiSyncApiType;
import com.fedstock.backend.ai.infrastructure.AiSyncRoundEntity;
import com.fedstock.backend.ai.infrastructure.AiSyncRoundJpaRepository;
import com.fedstock.backend.ai.infrastructure.AiSyncRoundStatus;
import com.fedstock.backend.auth.infrastructure.UserJpaRepository;
import com.fedstock.backend.main.error.BadRequestException;
import com.fedstock.backend.main.error.ConflictException;

@Service
public class AiClientSyncService {

    private static final Logger log = LoggerFactory.getLogger(AiClientSyncService.class);

    private static final String SCOPE_ALL_CLIENTS = "all_clients";
    private static final String SCOPE_SINGLE_CLIENT = "single_client";
    private static final String SCOPE_ALL_ALIAS = "all";
    private static final String SCOPE_SINGLE_ALIAS = "single";
    private static final Set<String> SUPPORTED_SCOPES = Set.of(
        SCOPE_ALL_CLIENTS,
        SCOPE_SINGLE_CLIENT,
        SCOPE_ALL_ALIAS,
        SCOPE_SINGLE_ALIAS
    );

    private final AiBackendClient aiBackendClient;
    private final UserJpaRepository userRepository;
    private final AiSyncRoundJpaRepository roundRepository;
    private final AiClusterAssignmentQueueJpaRepository clusterQueueRepository;
    private final AiFlModelQueueJpaRepository flModelQueueRepository;
    private final TransactionTemplate transactionTemplate;

    public AiClientSyncService(
        AiBackendClient aiBackendClient,
        UserJpaRepository userRepository,
        AiSyncRoundJpaRepository roundRepository,
        AiClusterAssignmentQueueJpaRepository clusterQueueRepository,
        AiFlModelQueueJpaRepository flModelQueueRepository,
        TransactionTemplate transactionTemplate
    ) {
        this.aiBackendClient = aiBackendClient;
        this.userRepository = userRepository;
        this.roundRepository = roundRepository;
        this.clusterQueueRepository = clusterQueueRepository;
        this.flModelQueueRepository = flModelQueueRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public ResponseEntity<?> assignCluster(ClusterAssignmentRequest request) {
        validateClusterAssignment(request);
        validateKnownClient(request.clientId());
        String scope = normalizeScope(request.scope());

        if (SCOPE_ALL_CLIENTS.equals(scope)) {
            log.info("⭐⭐⭐ all_clients cluster-assignment received roundId={} clientId={} expectedClientCount={}",
                request.roundId(),
                request.clientId(),
                request.expectedClientCount()
            );
        }

        if (SCOPE_SINGLE_CLIENT.equals(scope)) {
            return aiBackendClient.forwardClusterAssignment(canonicalClusterAssignmentRequest(request, scope));
        }

        ClusterBatchPlan plan = collectClusterAssignment(request);
        if (!plan.ready()) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(plan.queuedResponse());
        }

        try {
            log.info("⭐⭐⭐ all_clients cluster-assignment forwarding to AI roundId={} expectedClientCount={} receivedClientCount={}",
                request.roundId(),
                plan.batchRequest().expectedClientCount(),
                plan.batchRequest().clients().size()
            );
            ResponseEntity<JsonNode> response = aiBackendClient.forwardClusterAssignmentBatch(plan.batchRequest());
            completeRound(AiSyncApiType.CLUSTER_ASSIGNMENT, request.roundId(), response.getStatusCode().is2xxSuccessful(), response.getStatusCode().toString());
            return response;
        } catch (RuntimeException exception) {
            completeRound(AiSyncApiType.CLUSTER_ASSIGNMENT, request.roundId(), false, exception.getMessage());
            throw exception;
        }
    }

    public ResponseEntity<?> syncFlModel(
        String pathClientId,
        String bodyClientId,
        String scope,
        String roundId,
        Integer sampleWeight,
        MultipartFile modelFile
    ) {
        validateFlModel(pathClientId, bodyClientId, scope, roundId, sampleWeight, modelFile);
        validateKnownClient(pathClientId);
        String normalizedScope = normalizeScope(scope);

        if (SCOPE_ALL_CLIENTS.equals(normalizedScope)) {
            log.info("⭐⭐⭐ all_clients fl-model received roundId={} clientId={} sampleWeight={} filename={}",
                roundId,
                pathClientId,
                sampleWeight,
                modelFile.getOriginalFilename()
            );
        }

        if (SCOPE_SINGLE_CLIENT.equals(normalizedScope)) {
            return aiBackendClient.forwardFlModel(pathClientId, bodyClientId, normalizedScope, roundId, sampleWeight, modelFile);
        }

        FlModelBatchPlan plan = collectFlModel(pathClientId, roundId, sampleWeight, modelFile);
        if (!plan.ready()) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(plan.queuedResponse());
        }

        try {
            log.info("⭐⭐⭐ all_clients fl-model forwarding to AI roundId={} expectedClientCount={} receivedClientCount={}",
                roundId,
                plan.expectedClientCount(),
                plan.models().size()
            );
            ResponseEntity<JsonNode> response = aiBackendClient.forwardFlModelBatch(roundId, plan.expectedClientCount(), plan.models());
            completeRound(AiSyncApiType.FL_MODEL, roundId, response.getStatusCode().is2xxSuccessful(), response.getStatusCode().toString());
            return response;
        } catch (RuntimeException exception) {
            completeRound(AiSyncApiType.FL_MODEL, roundId, false, exception.getMessage());
            throw exception;
        }
    }

    public ResponseEntity<?> downloadFlModel(String clientId) {
        validateKnownClient(clientId);
        return aiBackendClient.downloadFlModel(clientId);
    }

    private void validateClusterAssignment(ClusterAssignmentRequest request) {
        validateScope(request.scope());
        if (request.featureNames().size() != request.featureImportance().size()) {
            throw new BadRequestException("featureNames and featureImportance must have the same length.");
        }
        if (request.expectedClientCount() != null && request.expectedClientCount() <= 0) {
            throw new BadRequestException("expectedClientCount must be positive.");
        }
    }

    private void validateFlModel(
        String pathClientId,
        String bodyClientId,
        String scope,
        String roundId,
        Integer sampleWeight,
        MultipartFile modelFile
    ) {
        if (pathClientId == null || pathClientId.isBlank()) {
            throw new BadRequestException("client_id path parameter is required.");
        }
        if (bodyClientId == null || bodyClientId.isBlank()) {
            throw new BadRequestException("client_id form field is required.");
        }
        if (!pathClientId.equals(bodyClientId)) {
            throw new BadRequestException("Path client_id and body client_id must match.");
        }
        validateScope(scope);
        if (roundId == null || roundId.isBlank()) {
            throw new BadRequestException("round_id is required.");
        }
        if (sampleWeight != null && sampleWeight <= 0) {
            throw new BadRequestException("sample_weight must be positive.");
        }
        if (modelFile == null || modelFile.isEmpty()) {
            throw new BadRequestException("model_file is required.");
        }
        String filename = modelFile.getOriginalFilename();
        if (filename == null || !filename.endsWith(".pt")) {
            throw new BadRequestException("model_file must be a .pt file.");
        }
    }

    private ClusterBatchPlan collectClusterAssignment(ClusterAssignmentRequest request) {
        return transactionTemplate.execute(status -> {
            AiSyncRoundEntity round = getOrCreateRound(AiSyncApiType.CLUSTER_ASSIGNMENT, request.roundId(), request.expectedClientCount());
            validateRoundCanAcceptClient(round, request.expectedClientCount());

            clusterQueueRepository.findByRoundIdAndClientId(request.roundId(), request.clientId())
                .ifPresentOrElse(
                    queued -> queued.updateFrom(request),
                    () -> clusterQueueRepository.save(new AiClusterAssignmentQueueEntity(request))
                );

            long receivedCount = clusterQueueRepository.countByRoundId(request.roundId());
            if (receivedCount > round.getExpectedClientCount()) {
                throw new ConflictException("Received client count exceeds expected client count.");
            }
            if (round.isFinished() || round.isForwarding()) {
                return ClusterBatchPlan.waiting(queuedResponse(
                    "cluster-assignment",
                    request.roundId(),
                    request.clientId(),
                    round.getExpectedClientCount(),
                    receivedCount,
                    round.getStatus().name(),
                    round.isFinished()
                ));
            }
            if (round.getStatus() == AiSyncRoundStatus.FAILED) {
                round.markCollecting();
            }
            if (receivedCount < round.getExpectedClientCount()) {
                return ClusterBatchPlan.waiting(queuedResponse(
                    "cluster-assignment",
                    request.roundId(),
                    request.clientId(),
                    round.getExpectedClientCount(),
                    receivedCount,
                    "QUEUED",
                    false
                ));
            }

            List<ClusterAssignmentBatchItem> clients = clusterQueueRepository.findByRoundIdOrderByClientIdAsc(request.roundId())
                .stream()
                .map(item -> new ClusterAssignmentBatchItem(
                    item.getClientId(),
                    item.getSampleCount(),
                    item.getFeatureNames(),
                    item.getFeatureImportance()
                ))
                .toList();
            round.markForwarding();
            return ClusterBatchPlan.ready(new ClusterAssignmentBatchRequest(
                SCOPE_ALL_CLIENTS,
                request.roundId(),
                round.getExpectedClientCount(),
                clients
            ));
        });
    }

    private FlModelBatchPlan collectFlModel(
        String clientId,
        String roundId,
        Integer sampleWeight,
        MultipartFile modelFile
    ) {
        byte[] fileBytes = readModelFile(modelFile);

        return transactionTemplate.execute(status -> {
            AiSyncRoundEntity round = getOrCreateRound(AiSyncApiType.FL_MODEL, roundId, null);
            validateRoundCanAcceptClient(round, null);

            flModelQueueRepository.findByRoundIdAndClientId(roundId, clientId)
                .ifPresentOrElse(
                    queued -> queued.updateFrom(sampleWeight, modelFile.getOriginalFilename(), modelFile.getContentType(), fileBytes),
                    () -> flModelQueueRepository.save(new AiFlModelQueueEntity(
                        roundId,
                        clientId,
                        sampleWeight,
                        modelFile.getOriginalFilename(),
                        modelFile.getContentType(),
                        fileBytes
                    ))
                );

            long receivedCount = flModelQueueRepository.countByRoundId(roundId);
            if (receivedCount > round.getExpectedClientCount()) {
                throw new ConflictException("Received client count exceeds expected client count.");
            }
            if (round.isFinished() || round.isForwarding()) {
                return FlModelBatchPlan.waiting(queuedResponse(
                    "fl-model",
                    roundId,
                    clientId,
                    round.getExpectedClientCount(),
                    receivedCount,
                    round.getStatus().name(),
                    round.isFinished()
                ));
            }
            if (round.getStatus() == AiSyncRoundStatus.FAILED) {
                round.markCollecting();
            }
            if (receivedCount < round.getExpectedClientCount()) {
                return FlModelBatchPlan.waiting(queuedResponse(
                    "fl-model",
                    roundId,
                    clientId,
                    round.getExpectedClientCount(),
                    receivedCount,
                    "QUEUED",
                    false
                ));
            }

            List<AiFlModelQueueEntity> models = flModelQueueRepository.findByRoundIdOrderByClientIdAsc(roundId);
            round.markForwarding();
            return FlModelBatchPlan.ready(round.getExpectedClientCount(), models);
        });
    }

    private AiSyncRoundEntity getOrCreateRound(AiSyncApiType apiType, String roundId, Integer providedExpectedClientCount) {
        return roundRepository.findForUpdate(apiType, roundId)
            .orElseGet(() -> roundRepository.save(new AiSyncRoundEntity(apiType, roundId, resolveExpectedClientCount(providedExpectedClientCount))));
    }

    private void validateRoundCanAcceptClient(AiSyncRoundEntity round, Integer providedExpectedClientCount) {
        if (providedExpectedClientCount != null && !providedExpectedClientCount.equals(round.getExpectedClientCount())) {
            throw new BadRequestException("expectedClientCount must match the server-side round client count.");
        }
    }

    private Integer resolveExpectedClientCount(Integer providedExpectedClientCount) {
        long userCount = userRepository.count();
        if (userCount <= 0) {
            throw new BadRequestException("No registered users are available for all_clients scope.");
        }
        if (userCount > Integer.MAX_VALUE) {
            throw new BadRequestException("Registered user count is too large.");
        }
        int expectedClientCount = (int) userCount;
        if (providedExpectedClientCount != null && providedExpectedClientCount != expectedClientCount) {
            throw new BadRequestException("expectedClientCount must match the registered user count.");
        }
        return expectedClientCount;
    }

    private byte[] readModelFile(MultipartFile modelFile) {
        try {
            return modelFile.getBytes();
        } catch (IOException exception) {
            throw new BadRequestException("model_file could not be read.");
        }
    }

    private void validateKnownClient(String clientId) {
        if (!userRepository.existsByStoreId(clientId)) {
            throw new BadRequestException("client_id is not registered.");
        }
    }

    private QueuedAiResponse queuedResponse(
        String api,
        String roundId,
        String clientId,
        Integer expectedClientCount,
        Long receivedClientCount,
        String status,
        boolean forwarded
    ) {
        return new QueuedAiResponse(
            api,
            SCOPE_ALL_CLIENTS,
            roundId,
            clientId,
            expectedClientCount,
            receivedClientCount,
            status,
            forwarded
        );
    }

    private void completeRound(AiSyncApiType apiType, String roundId, boolean successful, String message) {
        transactionTemplate.executeWithoutResult(status -> roundRepository.findForUpdate(apiType, roundId)
            .ifPresent(round -> {
                if (successful) {
                    round.markForwarded();
                } else {
                    round.markFailed(message);
                }
            }));
    }

    private void validateScope(String scope) {
        if (scope == null || !SUPPORTED_SCOPES.contains(scope)) {
            throw new BadRequestException("scope must be all_clients, all, single_client, or single.");
        }
    }

    private String normalizeScope(String scope) {
        return switch (scope) {
            case SCOPE_ALL_ALIAS -> SCOPE_ALL_CLIENTS;
            case SCOPE_SINGLE_ALIAS -> SCOPE_SINGLE_CLIENT;
            default -> scope;
        };
    }

    private ClusterAssignmentRequest canonicalClusterAssignmentRequest(ClusterAssignmentRequest request, String scope) {
        if (scope.equals(request.scope())) {
            return request;
        }
        return new ClusterAssignmentRequest(
            scope,
            request.roundId(),
            request.clientId(),
            request.sampleCount(),
            request.featureNames(),
            request.featureImportance(),
            request.expectedClientCount()
        );
    }

    private record ClusterBatchPlan(
        boolean ready,
        QueuedAiResponse queuedResponse,
        ClusterAssignmentBatchRequest batchRequest
    ) {
        private static ClusterBatchPlan waiting(QueuedAiResponse queuedResponse) {
            return new ClusterBatchPlan(false, queuedResponse, null);
        }

        private static ClusterBatchPlan ready(ClusterAssignmentBatchRequest batchRequest) {
            return new ClusterBatchPlan(true, null, batchRequest);
        }
    }

    private record FlModelBatchPlan(
        boolean ready,
        QueuedAiResponse queuedResponse,
        Integer expectedClientCount,
        List<AiFlModelQueueEntity> models
    ) {
        private static FlModelBatchPlan waiting(QueuedAiResponse queuedResponse) {
            return new FlModelBatchPlan(false, queuedResponse, null, List.of());
        }

        private static FlModelBatchPlan ready(Integer expectedClientCount, List<AiFlModelQueueEntity> models) {
            return new FlModelBatchPlan(true, null, expectedClientCount, models);
        }
    }
}
