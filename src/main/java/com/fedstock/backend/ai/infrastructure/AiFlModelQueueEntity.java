package com.fedstock.backend.ai.infrastructure;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "ai_fl_model_queue",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_ai_fl_model_round_client", columnNames = {"round_id", "client_id"})
    }
)
public class AiFlModelQueueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "round_id", nullable = false, length = 120)
    private String roundId;

    @Column(name = "client_id", nullable = false, length = 100)
    private String clientId;

    @Column(name = "sample_weight")
    private Integer sampleWeight;

    @Column(name = "filename", nullable = false, length = 255)
    private String filename;

    @Column(name = "content_type", length = 120)
    private String contentType;

    @Column(name = "model_file", nullable = false)
    private byte[] modelFile;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected AiFlModelQueueEntity() {
    }

    public AiFlModelQueueEntity(
        String roundId,
        String clientId,
        Integer sampleWeight,
        String filename,
        String contentType,
        byte[] modelFile
    ) {
        this.roundId = roundId;
        this.clientId = clientId;
        updateFrom(sampleWeight, filename, contentType, modelFile);
    }

    public String getClientId() {
        return clientId;
    }

    public Integer getSampleWeight() {
        return sampleWeight;
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getModelFile() {
        return modelFile;
    }

    public void updateFrom(Integer sampleWeight, String filename, String contentType, byte[] modelFile) {
        this.sampleWeight = sampleWeight;
        this.filename = filename;
        this.contentType = contentType;
        this.modelFile = modelFile;
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
