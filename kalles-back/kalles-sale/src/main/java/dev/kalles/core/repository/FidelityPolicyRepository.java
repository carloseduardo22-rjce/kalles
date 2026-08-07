package dev.kalles.core.repository;

import dev.kalles.core.entity.FidelityPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FidelityPolicyRepository extends JpaRepository<FidelityPolicy, UUID> {

    Optional<FidelityPolicy> findFirstByActiveTrue();

    Optional<FidelityPolicy> findFirstByCompanyIdAndActiveTrue(UUID companyId);

    List<FidelityPolicy> findAllByCompanyIdOrderByCreatedAtDesc(UUID companyId);

    @Modifying
    @Query("UPDATE FidelityPolicy fp SET fp.active = false WHERE fp.active = true")
    void deactivateAll();

    @Modifying
    @Query("UPDATE FidelityPolicy fp SET fp.active = false WHERE fp.active = true AND fp.companyId = :companyId")
    void deactivateAllByCompanyId(@Param("companyId") UUID companyId);
}
