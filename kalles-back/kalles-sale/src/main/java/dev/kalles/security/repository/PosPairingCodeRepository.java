package dev.kalles.security.repository;

import dev.kalles.security.entity.PosPairingCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PosPairingCodeRepository extends JpaRepository<PosPairingCode, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PosPairingCode> findByCodeHash(String codeHash);

    @Modifying
    @Query("update PosPairingCode c set c.usedAt = :now where c.cashRegisterId = :cashRegisterId and c.usedAt is null")
    void consumeUnusedCodes(@Param("cashRegisterId") UUID cashRegisterId, @Param("now") LocalDateTime now);
}
