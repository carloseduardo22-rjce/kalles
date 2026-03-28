package dev.kalles.sale.mercadopago.adapter.out.persistence.repository;

import dev.kalles.sale.mercadopago.adapter.out.persistence.entity.MercadoPagoCompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface SpringDataMercadoPagoCompanyRepository extends JpaRepository<MercadoPagoCompanyEntity, UUID> {
    Optional<MercadoPagoCompanyEntity> findByExternalId(String externalId);
    Optional<MercadoPagoCompanyEntity> findByTenantId(UUID tenantId);
}
