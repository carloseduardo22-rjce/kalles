package dev.kalles.security.repository;

import dev.kalles.security.entity.PosPairingCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PosPairingCodeRepository extends JpaRepository<PosPairingCode, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PosPairingCode> findByCodeHash(String codeHash);
}
