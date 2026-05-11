package com.fedstock.backend.product.infrastructure;

import java.time.LocalDateTime;

import com.fedstock.backend.store.infrastructure.StoreEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String category;

    @Column(nullable = false, length = 30)
    private String unit;

    @Column(nullable = false)
    private Integer safetyStock;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "product", fetch = FetchType.LAZY)
    private InventoryEntity inventory;

    protected ProductEntity() {
    }

    public ProductEntity(StoreEntity store, String name, String category, String unit, Integer safetyStock) {
        this.store = store;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.safetyStock = safetyStock;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public StoreEntity getStore() {
        return store;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getUnit() {
        return unit;
    }

    public Integer getSafetyStock() {
        return safetyStock;
    }

    public Boolean getActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public InventoryEntity getInventory() {
        return inventory;
    }

    public void update(String name, String category, String unit, Integer safetyStock, Boolean active) {
        if (name != null) {
            this.name = name;
        }
        if (category != null) {
            this.category = category;
        }
        if (unit != null) {
            this.unit = unit;
        }
        if (safetyStock != null) {
            this.safetyStock = safetyStock;
        }
        if (active != null) {
            this.active = active;
        }
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
