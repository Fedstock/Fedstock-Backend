package com.fedstock.backend.v1.forecast.api;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fedstock.backend.v1.forecast.application.ForecastService;

@RestController
@RequestMapping("/api/v1/forecast")
public class ForecastController {

    private final ForecastService forecastService;

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @PostMapping("/analyze-csv")
    public JsonNode analyzeCsv(
        @RequestParam MultipartFile file,
        @RequestParam(required = false) String storeId
    ) {
        return forecastService.analyzeCsv(file, storeId == null ? "default" : storeId);
    }

    @GetMapping("/results/{analysisId}")
    public JsonNode results(
        @PathVariable String analysisId
    ) {
        return forecastService.results(analysisId);
    }

    @GetMapping("/results/{analysisId}/products/{itemId}/chart")
    public JsonNode productChart(
        @PathVariable String analysisId,
        @PathVariable String itemId
    ) {
        return forecastService.productChart(analysisId, itemId);
    }

    @GetMapping("/results/{analysisId}/top-products")
    public JsonNode topProducts(
        @PathVariable String analysisId,
        @RequestParam(defaultValue = "8") int limit
    ) {
        return forecastService.topProducts(analysisId, limit);
    }

    @GetMapping("/results/{analysisId}/flow-change")
    public JsonNode flowChange(
        @PathVariable String analysisId,
        @RequestParam(defaultValue = "10") int limit
    ) {
        return forecastService.flowChange(analysisId, limit);
    }
}
