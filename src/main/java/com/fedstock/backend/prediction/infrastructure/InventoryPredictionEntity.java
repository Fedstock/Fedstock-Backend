package com.fedstock.backend.prediction.infrastructure;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fedstock.backend.product.infrastructure.ProductEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory_predictions")
public class InventoryPredictionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal predictedDailySales;

    @Column(nullable = false)
    private Integer currentQuantity;

    private LocalDate expectedStockoutDate;

    @Column(nullable = false)
    private Integer recommendationQuantity;

    @Column(nullable = false, updatable = false)
    private LocalDateTime predictedAt;

    protected InventoryPredictionEntity() {
    }

    public InventoryPredictionEntity(
        ProductEntity product,
        BigDecimal predictedDailySales,
        Integer currentQuantity,
        LocalDate expectedStockoutDate,
        Integer recommendationQuantity
    ) {
        this.product = product;
        this.predictedDailySales = predictedDailySales;
        this.currentQuantity = currentQuantity;
        this.expectedStockoutDate = expectedStockoutDate;
        this.recommendationQuantity = recommendationQuantity;
    }

    public Long getId() {
        return id;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public BigDecimal getPredictedDailySales() {
        return predictedDailySales;
    }

    public Integer getCurrentQuantity() {
        return currentQuantity;
    }

    public LocalDate getExpectedStockoutDate() {
        return expectedStockoutDate;
    }

    public Integer getRecommendationQuantity() {
        return recommendationQuantity;
    }

    public LocalDateTime getPredictedAt() {
        return predictedAt;
    }

    @PrePersist
    void prePersist() {
        predictedAt = LocalDateTime.now();
    }
}
