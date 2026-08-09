package dev.kalles.fidelity.repository;

import dev.kalles.fidelity.entity.Fidelity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FidelityRepository extends JpaRepository<Fidelity, UUID> {

    Optional<Fidelity> findByClientId(UUID clientId);

    boolean existsByClientId(UUID clientId);
}
