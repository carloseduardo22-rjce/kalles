package dev.kalles.support.infrastructure.persistence.repository;

import dev.kalles.support.infrastructure.persistence.entity.AgentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgentRepository extends JpaRepository<AgentEntity, UUID> {

    Optional<AgentEntity> findByTenantIdAndEmployeeId(UUID tenantId, String employeeId);

    List<AgentEntity> findAllByTenantIdAndActiveTrueOrderByNameAsc(UUID tenantId);

    List<AgentEntity> findAllByTenantIdOrderByNameAsc(UUID tenantId);

    Optional<AgentEntity> findByIdAndTenantId(UUID id, UUID tenantId);
}
