package com.fedstock.backend.auth.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findFirstByEmailOrStoreIdOrUsername(String email, String storeId, String username);

    boolean existsByEmail(String email);

    boolean existsByEmailOrStoreIdOrUsername(String email, String storeId, String username);

    boolean existsByStoreId(String storeId);
}
