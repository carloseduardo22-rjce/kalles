package dev.kalles.security.repository;

import dev.kalles.security.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByEmail(String email);
    List<Account> findAllByEmailIgnoreCase(String email);
    Optional<Account> findByTenantIdAndEmailIgnoreCase(UUID tenantId, String email);
    Optional<Account> findByIdAndTenantId(UUID id, UUID tenantId);
}
