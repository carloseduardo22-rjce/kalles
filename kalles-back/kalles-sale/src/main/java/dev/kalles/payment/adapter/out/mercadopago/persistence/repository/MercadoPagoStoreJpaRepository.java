package dev.kalles.payment.adapter.out.mercadopago.persistence.repository;

import dev.kalles.payment.adapter.out.mercadopago.persistence.entity.MercadoPagoStoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MercadoPagoStoreJpaRepository extends JpaRepository<MercadoPagoStoreEntity, UUID> {

    Optional<MercadoPagoStoreEntity> findByExternalReference(String externalReference);

    Optional<MercadoPagoStoreEntity> findByCompanyId(UUID companyId);
}
