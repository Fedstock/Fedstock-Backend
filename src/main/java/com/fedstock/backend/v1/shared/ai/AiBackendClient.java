package com.fedstock.backend.v1.shared.ai;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import com.fedstock.backend.main.error.BadGatewayException;

@Component
public class AiBackendClient {

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String backendUrl;

    public AiBackendClient(
        ObjectMapper objectMapper,
        RestClient.Builder restClientBuilder,
        @Value("${app.ai.backend-url:${AI_BACKEND_URL:http://localhost:8000}}") String backendUrl
    ) {
        this.objectMapper = objectMapper;
        this.backendUrl = trimTrailingSlash(backendUrl);
        this.restClient = restClientBuilder.build();
    }

    public JsonNode getHealth() {
        return forwardHealth().getBody();
    }

    public JsonNode analyzeCsv(MultipartFile file, String storeId) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new MultipartFileResource(file));
            if (storeId != null && !storeId.isBlank()) {
                body.add("storeId", storeId);
            }

            String response = restClient.post()
                .uri(aiUri("/analyze-csv"))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(String.class);

            return objectMapper.readTree(response);
        } catch (RestClientResponseException exception) {
            throw new BadGatewayException("AI server rejected the CSV analysis request.");
        } catch (RestClientException | IOException exception) {
            throw new BadGatewayException("AI server connection failed.");
        }
    }

    public ResponseEntity<JsonNode> forwardHealth() {
        try {
            return restClient.get()
                .uri(aiUri("/health"))
                .exchange((request, response) -> toJsonResponse(response.getStatusCode(), response.getHeaders(), response.getBody()));
        } catch (RestClientException exception) {
            throw new BadGatewayException("AI server connection failed.");
        }
    }

    public ResponseEntity<JsonNode> forwardAnalyzeCsv(MultipartFile file) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new MultipartFileResource(file));

            return restClient.post()
                .uri(aiUri("/analyze-csv"))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .exchange((request, response) -> toJsonResponse(response.getStatusCode(), response.getHeaders(), response.getBody()));
        } catch (RestClientException | IOException exception) {
            throw new BadGatewayException("AI server connection failed.");
        }
    }

    public ResponseEntity<JsonNode> forwardRegisterClient(
        String clientId,
        MultipartFile modelFile,
        MultipartFile importanceFile,
        String importanceJson,
        Integer sampleWeight
    ) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("model_file", new MultipartFileResource(modelFile));
            if (importanceFile != null && !importanceFile.isEmpty()) {
                body.add("importance_file", new MultipartFileResource(importanceFile));
            }
            if (importanceJson != null && !importanceJson.isBlank()) {
                body.add("importance_json", importanceJson);
            }
            if (sampleWeight != null) {
                body.add("sample_weight", sampleWeight);
            }

            return restClient.post()
                .uri(aiUri("/clients/register"))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .exchange((request, response) -> toJsonResponse(response.getStatusCode(), response.getHeaders(), response.getBody()));
        } catch (RestClientException | IOException exception) {
            throw new BadGatewayException("AI server connection failed.");
        }
    }

    public ResponseEntity<byte[]> forwardFlModel(String clientId) {
        try {
            return restClient.get()
                .uri(aiUri("/clients/" + encodePathSegment(clientId) + "/fl-model"))
                .exchange((request, response) -> {
                    byte[] payload = StreamUtils.copyToByteArray(response.getBody());
                    HttpHeaders sourceHeaders = response.getHeaders();
                    MediaType contentType = sourceHeaders.getContentType() == null
                        ? MediaType.APPLICATION_OCTET_STREAM
                        : sourceHeaders.getContentType();

                    ResponseEntity.BodyBuilder builder = ResponseEntity
                        .status(response.getStatusCode())
                        .contentType(contentType);

                    String disposition = sourceHeaders.getFirst(HttpHeaders.CONTENT_DISPOSITION);
                    if (disposition != null && !disposition.isBlank()) {
                        builder.header(HttpHeaders.CONTENT_DISPOSITION, disposition);
                    }

                    return builder.body(payload);
                });
        } catch (RestClientException exception) {
            throw new BadGatewayException("AI server connection failed.");
        }
    }

    private ResponseEntity<JsonNode> toJsonResponse(
        org.springframework.http.HttpStatusCode statusCode,
        HttpHeaders headers,
        java.io.InputStream body
    ) throws IOException {
        String payload = StreamUtils.copyToString(body, java.nio.charset.StandardCharsets.UTF_8);
        JsonNode responseBody = payload == null || payload.isBlank()
            ? objectMapper.createObjectNode()
            : objectMapper.readTree(payload);

        MediaType contentType = headers.getContentType() == null ? MediaType.APPLICATION_JSON : headers.getContentType();
        return ResponseEntity
            .status(statusCode)
            .contentType(contentType)
            .body(responseBody);
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:8000";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private URI aiUri(String path) {
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return URI.create(backendUrl + normalizedPath);
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
}
