package dev.kalles.sale.payment.adapter.out.mercadopago.persistence.repository;

import dev.kalles.sale.payment.adapter.out.mercadopago.persistence.entity.MercadoPagoTenantAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MercadoPagoTenantAccountJpaRepository extends JpaRepository<MercadoPagoTenantAccountEntity, UUID> {
}
