package dev.kalles.sale.repository;

import dev.kalles.sale.entity.SaleAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SaleAuditEventRepository extends JpaRepository<SaleAuditEvent, UUID> {
}
