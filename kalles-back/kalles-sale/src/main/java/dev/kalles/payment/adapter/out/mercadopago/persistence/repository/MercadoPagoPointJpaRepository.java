package dev.kalles.payment.adapter.out.mercadopago.persistence.repository;

import dev.kalles.payment.adapter.out.mercadopago.persistence.entity.MercadoPagoPointEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MercadoPagoPointJpaRepository extends JpaRepository<MercadoPagoPointEntity, UUID> {

    Optional<MercadoPagoPointEntity> findByExternalReference(String externalReference);

    Optional<MercadoPagoPointEntity> findFirstByCashRegisterId(UUID cashRegisterId);
}
