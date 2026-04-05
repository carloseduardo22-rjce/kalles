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

    @EntityGraph(attributePaths = {"user", "agent", "category", "interactions"})
    List<TicketEntity> findAllByStatusOrderByCreatedAtDesc(TicketStatus status);

    @EntityGraph(attributePaths = {"user", "agent", "category", "interactions"})
    List<TicketEntity> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"user", "agent", "category", "interactions"})
    List<TicketEntity> findAllByAgentIdOrderByCreatedAtDesc(UUID agentId);

    @EntityGraph(attributePaths = {"user", "agent", "category", "interactions"})
    List<TicketEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    @EntityGraph(attributePaths = {"user", "agent", "category", "interactions"})
    List<TicketEntity> findAllByUserEmailOrderByCreatedAtDesc(String email);

    @EntityGraph(attributePaths = {"user", "agent", "category", "interactions"})
    List<TicketEntity> findAllByUserEmailAndStatusOrderByCreatedAtDesc(String email, TicketStatus status);
}
