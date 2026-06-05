package com.fedstock.backend.v1.forecast.application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fedstock.backend.main.error.BadRequestException;
import com.fedstock.backend.v1.forecast.infrastructure.ForecastResultStore;
import com.fedstock.backend.v1.shared.ai.AiBackendClient;

@Service
public class ForecastService {

    private static final DateTimeFormatter ANALYSIS_ID_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final ObjectMapper objectMapper;
    private final AiBackendClient aiBackendClient;
    private final ForecastResultStore forecastResultStore;

    public ForecastService(
        ObjectMapper objectMapper,
        AiBackendClient aiBackendClient,
        ForecastResultStore forecastResultStore
    ) {
        this.objectMapper = objectMapper;
        this.aiBackendClient = aiBackendClient;
        this.forecastResultStore = forecastResultStore;
    }

    public JsonNode analyzeCsv(MultipartFile file, String storeId) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("CSV file is required.");
        }

        JsonNode result = aiBackendClient.analyzeCsv(file, storeId);
        String analysisId = text(result, "analysisId");
        if (analysisId == null) {
            analysisId = "analysis_" + LocalDateTime.now().format(ANALYSIS_ID_FORMATTER);
            ObjectNode copy = result.deepCopy();
            copy.put("analysisId", analysisId);
            result = copy;
        }

        forecastResultStore.save(analysisId, result);
        return result;
    }

    public JsonNode results(String analysisId) {
        return required(analysisId);
    }

    public JsonNode productChart(String analysisId, String itemId) {
        JsonNode result = required(analysisId);
        JsonNode chart = findByItemId(result.path("forecastDailySeries"), itemId);
        if (!chart.isMissingNode()) {
            return chart;
        }

        JsonNode item = findByItemId(result.path("forecastItems"), itemId);
        if (!item.isMissingNode()) {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("itemId", item.path("itemId").asText(itemId));
            response.put("itemName", item.path("itemName").asText(itemId));
            response.put("forecastQty", item.path("forecastQty").asDouble(0));
            response.put("forecastHorizonDays", result.path("forecastWindow").path("horizonDays").asInt(7));
            response.set("points", objectMapper.createArrayNode());
            response.put("minSales", 0);
            response.put("maxSales", 0);
            response.put("averageSales", 0);
            return response;
        }

        throw new NoSuchElementException("Forecast product chart not found.");
    }

    public JsonNode topProducts(String analysisId, int limit) {
        JsonNode result = required(analysisId);
        ObjectNode response = objectMapper.createObjectNode();
        response.set("items", limit(result.path("topForecastProducts"), limit));
        response.set("categoryMix", copyArray(result.path("categoryMix")));
        return response;
    }

    public JsonNode flowChange(String analysisId, int limit) {
        JsonNode result = required(analysisId);
        ObjectNode response = objectMapper.createObjectNode();
        response.set("items", limit(result.path("flowChangeChart"), limit));
        return response;
    }

    private JsonNode required(String analysisId) {
        return forecastResultStore.find(analysisId)
            .orElseThrow(() -> new NoSuchElementException("Forecast result not found."));
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isTextual() && !value.asText().isBlank() ? value.asText() : null;
    }

    private JsonNode findByItemId(JsonNode items, String itemId) {
        if (!items.isArray()) {
            return objectMapper.missingNode();
        }
        for (JsonNode item : items) {
            if (itemId.equals(item.path("itemId").asText())) {
                return item;
            }
        }
        return objectMapper.missingNode();
    }

    private ArrayNode limit(JsonNode source, int limit) {
        ArrayNode target = objectMapper.createArrayNode();
        if (!source.isArray()) {
            return target;
        }

        int safeLimit = Math.max(0, limit);
        for (JsonNode item : source) {
            if (target.size() >= safeLimit) {
                break;
            }
            target.add(item);
        }
        return target;
    }

    private ArrayNode copyArray(JsonNode source) {
        ArrayNode target = objectMapper.createArrayNode();
        if (!source.isArray()) {
            return target;
        }
        source.forEach(target::add);
        return target;
    }
}
