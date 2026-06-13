package com.fedstock.backend.ai.infrastructure;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "ai_sync_rounds",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_ai_sync_rounds_api_round", columnNames = {"api_type", "round_id"})
    }
)
public class AiSyncRoundEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "api_type", nullable = false, length = 50)
    private AiSyncApiType apiType;

    @Column(name = "round_id", nullable = false, length = 120)
    private String roundId;

    @Column(name = "expected_client_count", nullable = false)
    private Integer expectedClientCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AiSyncRoundStatus status;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "forwarded_at")
    private LocalDateTime forwardedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected AiSyncRoundEntity() {
    }

    public AiSyncRoundEntity(AiSyncApiType apiType, String roundId, Integer expectedClientCount) {
        this.apiType = apiType;
        this.roundId = roundId;
        this.expectedClientCount = expectedClientCount;
        this.status = AiSyncRoundStatus.COLLECTING;
    }

    public Integer getExpectedClientCount() {
        return expectedClientCount;
    }

    public AiSyncRoundStatus getStatus() {
        return status;
    }

    public boolean isFinished() {
        return status == AiSyncRoundStatus.FORWARDED;
    }

    public boolean isForwarding() {
        return status == AiSyncRoundStatus.FORWARDING;
    }

    public void markCollecting() {
        this.status = AiSyncRoundStatus.COLLECTING;
        this.lastError = null;
    }

    public void markForwarding() {
        this.status = AiSyncRoundStatus.FORWARDING;
        this.lastError = null;
    }

    public void markForwarded() {
        this.status = AiSyncRoundStatus.FORWARDED;
        this.lastError = null;
        this.forwardedAt = LocalDateTime.now();
    }

    public void markFailed(String message) {
        this.status = AiSyncRoundStatus.FAILED;
        this.lastError = message == null ? "AI forwarding failed." : message;
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
