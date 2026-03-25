package dev.kalles.sale.security.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountVerificationRepository extends JpaRepository<AccountVerification, UUID> {
    Optional<AccountVerification> findFirstByAccountIdAndCodeOrderByCreatedAtDesc(UUID accountId, String code);
    Optional<AccountVerification> findFirstByAccountIdOrderByCreatedAtDesc(UUID accountId);
}
