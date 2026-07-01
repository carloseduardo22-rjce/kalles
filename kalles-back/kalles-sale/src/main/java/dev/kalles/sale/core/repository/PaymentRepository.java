package dev.kalles.sale.core.repository;

import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.kalles.sale.core.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findAllBySaleId(UUID saleId);
}
