package dev.kalles.support.infrastructure.persistence.repository;

import dev.kalles.support.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByTenantIdAndEmailIgnoreCase(UUID tenantId, String email);

    List<UserEntity> findAllByTenantIdOrderByNameAsc(UUID tenantId);

    Optional<UserEntity> findByIdAndTenantId(UUID id, UUID tenantId);
}
