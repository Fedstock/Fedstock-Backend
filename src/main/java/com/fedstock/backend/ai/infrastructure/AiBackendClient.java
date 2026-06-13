package com.fedstock.backend.ai.infrastructure;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import com.fedstock.backend.ai.api.dto.ClusterAssignmentRequest;
import com.fedstock.backend.ai.api.dto.ClusterAssignmentBatchRequest;
import com.fedstock.backend.main.error.BadGatewayException;

@Component
public class AiBackendClient {

    private static final Logger log = LoggerFactory.getLogger(AiBackendClient.class);

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String backendUrl;

    public AiBackendClient(
        ObjectMapper objectMapper,
        RestClient.Builder restClientBuilder,
        @Value("${app.ai.backend-url:${AI_BACKEND_URL:http://localhost:8000}}") String backendUrl
    ) {
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder.build();
        this.backendUrl = trimTrailingSlash(backendUrl);
    }

    public ResponseEntity<JsonNode> forwardClusterAssignment(ClusterAssignmentRequest requestBody) {
        try {
            String payload = objectMapper.writeValueAsString(requestBody);
            return restClient.post()
                .uri(aiUri("/clients/cluster-assignment"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .exchange((request, response) -> toJsonResponse(response.getStatusCode(), response.getHeaders(), response.getBody()));
        } catch (RestClientException | IOException exception) {
            throw new BadGatewayException("AI server connection failed.");
        }
    }

    public ResponseEntity<JsonNode> forwardClusterAssignmentBatch(ClusterAssignmentBatchRequest requestBody) {
        try {
            String payload = objectMapper.writeValueAsString(requestBody);
            log.info("⭐⭐⭐ all_clients AI request POST /clients/cluster-assignment/batch roundId={} clientCount={}",
                requestBody.roundId(),
                requestBody.clients().size()
            );
            return restClient.post()
                .uri(aiUri("/clients/cluster-assignment/batch"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .exchange((request, response) -> toJsonResponse(response.getStatusCode(), response.getHeaders(), response.getBody()));
        } catch (RestClientException | IOException exception) {
            throw new BadGatewayException("AI server connection failed.");
        }
    }

    public ResponseEntity<byte[]> forwardFlModel(
        String pathClientId,
        String bodyClientId,
        String scope,
        String roundId,
        Integer sampleWeight,
        MultipartFile modelFile
    ) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("client_id", bodyClientId);
            body.add("scope", scope);
            body.add("round_id", roundId);
            if (sampleWeight != null) {
                body.add("sample_weight", sampleWeight);
            }
            body.add("model_file", new MultipartFileResource(modelFile));

            return restClient.post()
                .uri(aiUri("/clients/" + encodePathSegment(pathClientId) + "/fl-model"))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .exchange((request, response) -> {
                    byte[] payload = StreamUtils.copyToByteArray(response.getBody());
                    ResponseEntity.BodyBuilder builder = ResponseEntity
                        .status(response.getStatusCode())
                        .contentType(response.getHeaders().getContentType() == null
                            ? MediaType.APPLICATION_OCTET_STREAM
                            : response.getHeaders().getContentType());

                    copyHeader(response.getHeaders(), builder, HttpHeaders.CONTENT_DISPOSITION);
                    response.getHeaders().forEach((name, values) -> {
                        if (name.toLowerCase(java.util.Locale.ROOT).startsWith("x-fedstock-")) {
                            values.forEach(value -> builder.header(name, value));
                        }
                    });

                    return builder.body(payload);
                });
        } catch (RestClientException | IOException exception) {
            throw new BadGatewayException("AI server connection failed.");
        }
    }

    public ResponseEntity<JsonNode> forwardFlModelBatch(
        String roundId,
        Integer expectedClientCount,
        java.util.List<AiFlModelQueueEntity> models
    ) {
        try {
            log.info("⭐⭐⭐ all_clients AI request POST /clients/fl-model/batch roundId={} clientCount={}",
                roundId,
                models.size()
            );
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            String metadata = objectMapper.writeValueAsString(java.util.Map.of(
                "scope", "all_clients",
                "round_id", roundId,
                "expected_client_count", expectedClientCount,
                "models", models.stream()
                    .map(model -> java.util.Map.of(
                        "client_id", model.getClientId(),
                        "sample_weight", model.getSampleWeight() == null ? 0 : model.getSampleWeight(),
                        "filename", model.getFilename()
                    ))
                    .toList()
            ));

            HttpHeaders metadataHeaders = new HttpHeaders();
            metadataHeaders.setContentType(MediaType.APPLICATION_JSON);
            body.add("metadata", new HttpEntity<>(metadata, metadataHeaders));

            for (AiFlModelQueueEntity model : models) {
                HttpHeaders fileHeaders = new HttpHeaders();
                fileHeaders.setContentType(model.getContentType() == null || model.getContentType().isBlank()
                    ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.parseMediaType(model.getContentType()));
                body.add("model_files", new HttpEntity<>(new StoredModelResource(model.getModelFile(), model.getFilename()), fileHeaders));
            }

            return restClient.post()
                .uri(aiUri("/clients/fl-model/batch"))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .exchange((request, response) -> toJsonResponse(response.getStatusCode(), response.getHeaders(), response.getBody()));
        } catch (RestClientException | IOException exception) {
            throw new BadGatewayException("AI server connection failed.");
        }
    }

    private ResponseEntity<JsonNode> toJsonResponse(
        org.springframework.http.HttpStatusCode statusCode,
        HttpHeaders headers,
        java.io.InputStream body
    ) throws IOException {
        String payload = StreamUtils.copyToString(body, StandardCharsets.UTF_8);
        JsonNode responseBody = payload == null || payload.isBlank()
            ? objectMapper.createObjectNode()
            : objectMapper.readTree(payload);

        MediaType contentType = headers.getContentType() == null ? MediaType.APPLICATION_JSON : headers.getContentType();
        return ResponseEntity
            .status(statusCode)
            .contentType(contentType)
            .body(responseBody);
    }

    private void copyHeader(HttpHeaders source, ResponseEntity.BodyBuilder target, String headerName) {
        String value = source.getFirst(headerName);
        if (value != null && !value.isBlank()) {
            target.header(headerName, value);
        }
    }

    private URI aiUri(String path) {
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return URI.create(backendUrl + normalizedPath);
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:8000";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String encodePathSegment(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static class MultipartFileResource extends ByteArrayResource {

        private final String filename;

        MultipartFileResource(MultipartFile file) throws IOException {
            super(file.getBytes());
            this.filename = file.getOriginalFilename();
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }

    private static class StoredModelResource extends ByteArrayResource {

        private final String filename;

        StoredModelResource(byte[] bytes, String filename) {
            super(bytes);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}
