package com.fedstock.backend.ai.infrastructure;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiSyncRoundJpaRepository extends JpaRepository<AiSyncRoundEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select r
        from AiSyncRoundEntity r
        where r.apiType = :apiType
          and r.roundId = :roundId
        """)
    Optional<AiSyncRoundEntity> findForUpdate(
        @Param("apiType") AiSyncApiType apiType,
        @Param("roundId") String roundId
    );
}
