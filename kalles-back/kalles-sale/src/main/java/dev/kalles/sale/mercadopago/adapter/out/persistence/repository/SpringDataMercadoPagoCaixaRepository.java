package dev.kalles.sale.mercadopago.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import dev.kalles.sale.mercadopago.adapter.out.persistence.entity.MercadoPagoCaixaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataMercadoPagoCaixaRepository extends JpaRepository<MercadoPagoCaixaEntity, java.util.UUID> {
    Optional<MercadoPagoCaixaEntity> findByExternalId(String externalId);
}
