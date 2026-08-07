package dev.kalles.cashregister.repository;

import dev.kalles.cashregister.entity.CashRegisterClosing;
import dev.kalles.cashregister.entity.CashRegisterSession;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CashRegisterClosingRepository extends JpaRepository<CashRegisterClosing, UUID> {

    @EntityGraph(attributePaths = {"authorizedByOperator", "paymentTotals"})
    Optional<CashRegisterClosing> findBySession(CashRegisterSession session);
}
