package com.fedstock.backend.ai.infrastructure;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.fedstock.backend.ai.api.dto.ClusterAssignmentRequest;

@Entity
@Table(
    name = "ai_cluster_assignment_queue",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_ai_cluster_assignment_round_client", columnNames = {"round_id", "client_id"})
    }
)
public class AiClusterAssignmentQueueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "round_id", nullable = false, length = 120)
    private String roundId;

    @Column(name = "client_id", nullable = false, length = 100)
    private String clientId;

    @Column(name = "sample_count", nullable = false)
    private Integer sampleCount;

    @ElementCollection
    @CollectionTable(
        name = "ai_cluster_assignment_feature_names",
        joinColumns = @JoinColumn(name = "queue_id")
    )
    @OrderColumn(name = "sort_order")
    @Column(name = "feature_name", nullable = false, length = 120)
    private List<String> featureNames = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
        name = "ai_cluster_assignment_feature_importance",
        joinColumns = @JoinColumn(name = "queue_id")
    )
    @OrderColumn(name = "sort_order")
    @Column(name = "feature_importance", nullable = false, precision = 18, scale = 10)
    private List<BigDecimal> featureImportance = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected AiClusterAssignmentQueueEntity() {
    }

    public AiClusterAssignmentQueueEntity(ClusterAssignmentRequest request) {
        this.roundId = request.roundId();
        this.clientId = request.clientId();
        updateFrom(request);
    }

    public String getClientId() {
        return clientId;
    }

    public Integer getSampleCount() {
        return sampleCount;
    }

    public List<String> getFeatureNames() {
        return List.copyOf(featureNames);
    }

    public List<BigDecimal> getFeatureImportance() {
        return List.copyOf(featureImportance);
    }

    public void updateFrom(ClusterAssignmentRequest request) {
        this.sampleCount = request.sampleCount();
        this.featureNames.clear();
        this.featureNames.addAll(request.featureNames());
        this.featureImportance.clear();
        this.featureImportance.addAll(request.featureImportance());
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
