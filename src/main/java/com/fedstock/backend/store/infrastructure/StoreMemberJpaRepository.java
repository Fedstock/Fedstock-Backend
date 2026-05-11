package com.fedstock.backend.store.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreMemberJpaRepository extends JpaRepository<StoreMemberEntity, Long> {

    @EntityGraph(attributePaths = {"store"})
    @Query("select sm from StoreMemberEntity sm where sm.user.id = :userId order by sm.store.createdAt desc")
    List<StoreMemberEntity> findStoresByUserId(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"store"})
    @Query("select sm from StoreMemberEntity sm where sm.store.id = :storeId and sm.user.id = :userId")
    Optional<StoreMemberEntity> findByStoreIdAndUserId(@Param("storeId") Long storeId, @Param("userId") Long userId);

    boolean existsByStore_IdAndUser_Id(Long storeId, Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<StoreMemberEntity> findByStore_IdOrderByCreatedAtAsc(Long storeId);
}
