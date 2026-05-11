package com.fedstock.backend.sale.infrastructure;

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
@Table(name = "sales")
public class SaleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(nullable = false)
    private Integer soldQuantity;

    @Column(nullable = false)
    private LocalDateTime soldAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected SaleEntity() {
    }

    public SaleEntity(ProductEntity product, Integer soldQuantity, LocalDateTime soldAt) {
        this.product = product;
        this.soldQuantity = soldQuantity;
        this.soldAt = soldAt;
    }

    public Long getId() {
        return id;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public Integer getSoldQuantity() {
        return soldQuantity;
    }

    public LocalDateTime getSoldAt() {
        return soldAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (soldAt == null) {
            soldAt = now;
        }
        createdAt = now;
    }
}
