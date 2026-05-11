package com.fedstock.backend.prediction.api;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fedstock.backend.auth.api.CurrentUser;
import com.fedstock.backend.auth.application.UserPrincipal;
import com.fedstock.backend.prediction.api.dto.CreatePredictionRequest;
import com.fedstock.backend.prediction.api.dto.CreatedPredictionResponse;
import com.fedstock.backend.prediction.api.dto.PredictionResponse;
import com.fedstock.backend.prediction.application.PredictionService;

@RestController
@RequestMapping("/api/stores/{storeId}/predictions")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @GetMapping("/latest")
    public List<PredictionResponse> findLatest(
        @CurrentUser UserPrincipal user,
        @PathVariable Long storeId
    ) {
        return predictionService.findLatest(user.id(), storeId)
            .stream()
            .map(PredictionResponse::from)
            .toList();
    }

    @GetMapping
    public List<PredictionResponse> search(
        @CurrentUser UserPrincipal user,
        @PathVariable Long storeId,
        @RequestParam(required = false) Long productId,
        @RequestParam(name = "product_id", required = false) Long productIdAlias,
        @RequestParam(required = false) Integer limit
    ) {
        Long selectedProductId = productId == null ? productIdAlias : productId;
        return predictionService.search(user.id(), storeId, selectedProductId, limit)
            .stream()
            .map(PredictionResponse::from)
            .toList();
    }

    @PostMapping
    public ResponseEntity<CreatedPredictionResponse> create(
        @CurrentUser UserPrincipal user,
        @PathVariable Long storeId,
        @Valid @RequestBody CreatePredictionRequest request
    ) {
        CreatedPredictionResponse response = CreatedPredictionResponse.from(predictionService.create(
            user.id(),
            storeId,
            request.productId(),
            request.predictedDailySales(),
            request.currentQuantity(),
            request.expectedStockoutDate(),
            request.recommendationQuantity()
        ));
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{predictionId}")
            .buildAndExpand(response.id())
            .toUri();

        return ResponseEntity.created(location).body(response);
    }
}
