package dev.kalles.core.repository;

import dev.kalles.core.entity.SaleAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SaleAuditEventRepository extends JpaRepository<SaleAuditEvent, UUID> {
}
