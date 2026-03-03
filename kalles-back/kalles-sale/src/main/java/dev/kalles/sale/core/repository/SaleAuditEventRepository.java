package dev.kalles.sale.core.repository;

import dev.kalles.sale.core.entity.SaleAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SaleAuditEventRepository extends JpaRepository<SaleAuditEvent, UUID> {
}
