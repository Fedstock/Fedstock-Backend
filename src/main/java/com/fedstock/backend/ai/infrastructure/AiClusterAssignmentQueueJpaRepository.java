package com.fedstock.backend.ai.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AiClusterAssignmentQueueJpaRepository extends JpaRepository<AiClusterAssignmentQueueEntity, Long> {

    Optional<AiClusterAssignmentQueueEntity> findByRoundIdAndClientId(String roundId, String clientId);

    long countByRoundId(String roundId);

    List<AiClusterAssignmentQueueEntity> findByRoundIdOrderByClientIdAsc(String roundId);
}
