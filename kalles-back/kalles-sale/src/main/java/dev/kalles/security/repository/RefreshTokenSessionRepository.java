package dev.kalles.security.repository;

import dev.kalles.security.entity.RefreshTokenSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenSessionRepository extends JpaRepository<RefreshTokenSession, UUID> {
    Optional<RefreshTokenSession> findByTokenHash(String tokenHash);
    List<RefreshTokenSession> findAllByAccountIdAndRevokedAtIsNullAndExpiresAtAfter(UUID accountId, LocalDateTime reference);
}
