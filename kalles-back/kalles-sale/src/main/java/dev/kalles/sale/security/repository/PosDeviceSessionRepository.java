package dev.kalles.sale.security.repository;

import dev.kalles.sale.security.domain.PosDeviceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PosDeviceSessionRepository extends JpaRepository<PosDeviceSession, UUID> {
    
    Optional<PosDeviceSession> findByTokenAndActiveTrueAndExpiresAtGreaterThan(String token, LocalDateTime now);
}
