package com.fedstock.backend.prediction.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fedstock.backend.main.error.BadRequestException;
import com.fedstock.backend.prediction.infrastructure.InventoryPredictionEntity;
import com.fedstock.backend.prediction.infrastructure.InventoryPredictionJpaRepository;
import com.fedstock.backend.product.application.ProductService;
import com.fedstock.backend.product.infrastructure.ProductEntity;
import com.fedstock.backend.store.application.StoreService;

@Service
public class PredictionService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;

    private final InventoryPredictionJpaRepository predictionRepository;
    private final StoreService storeService;
    private final ProductService productService;

    public PredictionService(
        InventoryPredictionJpaRepository predictionRepository,
        StoreService storeService,
        ProductService productService
    ) {
        this.predictionRepository = predictionRepository;
        this.storeService = storeService;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public List<InventoryPredictionEntity> findLatest(Long userId, Long storeId) {
        storeService.requireMember(userId, storeId);
        return predictionRepository.findLatestByStoreId(storeId);
    }

    @Transactional(readOnly = true)
    public List<InventoryPredictionEntity> search(Long userId, Long storeId, Long productId, Integer limit) {
        storeService.requireMember(userId, storeId);
        return predictionRepository.search(storeId, productId, PageRequest.of(0, normalizedLimit(limit)));
    }

    @Transactional
    public InventoryPredictionEntity create(
        Long userId,
        Long storeId,
        Long productId,
        BigDecimal predictedDailySales,
        Integer currentQuantity,
        LocalDate expectedStockoutDate,
        Integer recommendationQuantity
    ) {
        storeService.requireOwner(userId, storeId);
        ProductEntity product = productService.requireProduct(storeId, productId);

        return predictionRepository.save(new InventoryPredictionEntity(
            product,
            requireNonNegative(predictedDailySales, "predictedDailySales"),
            requireNonNegative(currentQuantity, "currentQuantity"),
            expectedStockoutDate,
            recommendationQuantity == null ? 0 : requireNonNegative(recommendationQuantity, "recommendationQuantity")
        ));
    }

    private BigDecimal requireNonNegative(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(fieldName + " must be 0 or greater.");
        }
        return value;
    }

    private Integer requireNonNegative(Integer value, String fieldName) {
        if (value == null || value < 0) {
            throw new BadRequestException(fieldName + " must be 0 or greater.");
        }
        return value;
    }

    private int normalizedLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < 1) {
            throw new BadRequestException("limit must be greater than 0.");
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
