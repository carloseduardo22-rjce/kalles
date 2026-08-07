package dev.kalles.cashregister.repository;

import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.entity.CashRegisterSession;
import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.valueobject.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CashRegisterSessionRepository extends JpaRepository<CashRegisterSession, UUID> {

    Optional<CashRegisterSession> findByCashRegisterAndStatus(
        CashRegister cashRegister,
        SessionStatus status
    );

    boolean existsByCashRegisterAndStatus(
        CashRegister cashRegister,
        SessionStatus status
    );

    boolean existsByOperatorAndStatus(
        Operator operator,
        SessionStatus status
    );

    Optional<CashRegisterSession> findByIdAndCashRegister_CompanyId(UUID id, UUID companyId);

    List<CashRegisterSession> findBySessionPeriod_OpenedAtBetweenOrderBySessionPeriod_OpenedAtDesc(
        LocalDateTime start,
        LocalDateTime end
    );

    List<CashRegisterSession> findByCashRegister_CompanyIdAndSessionPeriod_OpenedAtBetweenOrderBySessionPeriod_OpenedAtDesc(
        UUID companyId,
        LocalDateTime start,
        LocalDateTime end
    );
}
