package dev.kalles.support.repository;

import dev.kalles.support.domain.TicketStatus;
import dev.kalles.support.entity.TicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<TicketEntity, UUID> {

    @EntityGraph(attributePaths = {"user", "agent", "category", "interactions"})
    Optional<TicketEntity> findWithDetailsByIdAndTenantId(UUID id, UUID tenantId);

    @EntityGraph(attributePaths = {"user", "agent", "category", "interactions"})
    List<TicketEntity> findAllByTenantIdAndStatusOrderByCreatedAtDesc(UUID tenantId, TicketStatus status);

    @EntityGraph(attributePaths = {"user", "agent", "category", "interactions"})
    Page<TicketEntity> findAllByTenantIdAndStatusOrderByCreatedAtDesc(UUID tenantId, TicketStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "agent", "category", "interactions"})
    List<TicketEntity> findAllByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    @EntityGraph(attributePaths = {"user", "agent", "category", "interactions"})
    Page<TicketEntity> findAllByTenantIdOrderByCreatedAtDesc(UUID tenantId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "agent", "category", "interactions"})
    List<TicketEntity> findAllByTenantIdAndAgentIdOrderByCreatedAtDesc(UUID tenantId, UUID agentId);

    @EntityGraph(attributePaths = {"user", "agent", "category", "interactions"})
    List<TicketEntity> findAllByTenantIdAndUserIdOrderByCreatedAtDesc(UUID tenantId, UUID userId);

    @EntityGraph(attributePaths = {"user", "agent", "category", "interactions"})
    List<TicketEntity> findAllByTenantIdAndUserEmailIgnoreCaseOrderByCreatedAtDesc(UUID tenantId, String email);

    @EntityGraph(attributePaths = {"user", "agent", "category", "interactions"})
    Page<TicketEntity> findAllByTenantIdAndUserEmailIgnoreCaseOrderByCreatedAtDesc(UUID tenantId, String email, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "agent", "category", "interactions"})
    List<TicketEntity> findAllByTenantIdAndUserEmailIgnoreCaseAndStatusOrderByCreatedAtDesc(UUID tenantId, String email, TicketStatus status);

    @EntityGraph(attributePaths = {"user", "agent", "category", "interactions"})
    Page<TicketEntity> findAllByTenantIdAndUserEmailIgnoreCaseAndStatusOrderByCreatedAtDesc(
            UUID tenantId,
            String email,
            TicketStatus status,
            Pageable pageable
    );
}
