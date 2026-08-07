package dev.kalles.core.repository;

import dev.kalles.core.entity.Fidelity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FidelityRepository extends JpaRepository<Fidelity, UUID> {

    Optional<Fidelity> findByClientId(UUID clientId);

    boolean existsByClientId(UUID clientId);
}
