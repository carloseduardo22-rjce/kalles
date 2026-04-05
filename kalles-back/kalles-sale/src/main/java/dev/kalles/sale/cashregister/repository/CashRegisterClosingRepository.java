package dev.kalles.sale.cashregister.repository;

import dev.kalles.sale.cashregister.entity.CashRegisterClosing;
import dev.kalles.sale.cashregister.entity.CashRegisterSession;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CashRegisterClosingRepository extends JpaRepository<CashRegisterClosing, UUID> {

    @EntityGraph(attributePaths = {"authorizedByOperator", "paymentTotals"})
    Optional<CashRegisterClosing> findBySession(CashRegisterSession session);
}
