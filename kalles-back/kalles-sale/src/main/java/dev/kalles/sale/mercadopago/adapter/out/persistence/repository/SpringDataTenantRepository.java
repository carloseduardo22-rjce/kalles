package dev.kalles.sale.mercadopago.adapter.out.persistence.repository;

import dev.kalles.sale.mercadopago.adapter.out.persistence.entity.MercadoPagoTenantConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataTenantRepository extends JpaRepository<MercadoPagoTenantConfigEntity, UUID> {
}
