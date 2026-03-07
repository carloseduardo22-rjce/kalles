package dev.kalles.sale.core.repository;

import dev.kalles.sale.core.entity.FidelityPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface FidelityPolicyRepository extends JpaRepository<FidelityPolicy, UUID> {

    Optional<FidelityPolicy> findFirstByActiveTrue();

    @Modifying
    @Query("UPDATE FidelityPolicy fp SET fp.active = false WHERE fp.active = true")
    void deactivateAll();
}
