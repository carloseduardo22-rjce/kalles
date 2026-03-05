package dev.kalles.sale.cashregister.repository;

import dev.kalles.sale.cashregister.entity.CashRegister;
import dev.kalles.sale.cashregister.entity.CashRegisterSession;
import dev.kalles.sale.cashregister.entity.Operator;
import dev.kalles.sale.cashregister.valueobject.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
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

    /** Verifica se o operador já está vinculado a alguma sessão ativa. */
    boolean existsByOperatorAndStatus(
        Operator operator,
        SessionStatus status
    );
}
