package dev.kalles.security.repository;

import dev.kalles.security.entity.PosDeviceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PosDeviceSessionRepository extends JpaRepository<PosDeviceSession, UUID> {

    Optional<PosDeviceSession> findByTokenHashAndActiveTrue(String tokenHash);

    Optional<PosDeviceSession> findByPosIdAndActiveTrue(UUID posId);
}
