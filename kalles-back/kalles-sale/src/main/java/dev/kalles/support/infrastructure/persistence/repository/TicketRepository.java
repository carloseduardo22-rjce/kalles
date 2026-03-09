package dev.kalles.support.infrastructure.persistence.repository;

import dev.kalles.support.domain.TicketStatus;
import dev.kalles.support.infrastructure.persistence.entity.TicketEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<TicketEntity, UUID> {

    @EntityGraph(attributePaths = {"user", "agent", "category", "interactions"})
    Optional<TicketEntity> findWithDetailsById(UUID id);

    List<TicketEntity> findAllByStatusOrderByCreatedAtDesc(TicketStatus status);

    List<TicketEntity> findAllByOrderByCreatedAtDesc();

    List<TicketEntity> findAllByAgentIdOrderByCreatedAtDesc(UUID agentId);

    List<TicketEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
}
