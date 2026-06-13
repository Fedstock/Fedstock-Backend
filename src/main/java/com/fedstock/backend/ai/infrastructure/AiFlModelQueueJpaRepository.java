package com.fedstock.backend.ai.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AiFlModelQueueJpaRepository extends JpaRepository<AiFlModelQueueEntity, Long> {

    Optional<AiFlModelQueueEntity> findByRoundIdAndClientId(String roundId, String clientId);

    long countByRoundId(String roundId);

    List<AiFlModelQueueEntity> findByRoundIdOrderByClientIdAsc(String roundId);
}
