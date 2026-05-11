package com.fedstock.backend.store.infrastructure;

import java.time.LocalDateTime;

import com.fedstock.backend.auth.infrastructure.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "store_members",
    uniqueConstraints = @UniqueConstraint(name = "uq_store_members_user_store", columnNames = {"user_id", "store_id"})
)
public class StoreMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StoreRole role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected StoreMemberEntity() {
    }

    public StoreMemberEntity(UserEntity user, StoreEntity store, StoreRole role) {
        this.user = user;
        this.store = store;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public StoreEntity getStore() {
        return store;
    }

    public StoreRole getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
