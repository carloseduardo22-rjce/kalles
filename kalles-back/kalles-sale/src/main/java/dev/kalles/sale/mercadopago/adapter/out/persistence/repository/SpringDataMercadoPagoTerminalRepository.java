package dev.kalles.sale.mercadopago.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.kalles.sale.mercadopago.adapter.out.persistence.entity.TerminalEntity;

@Repository
public interface SpringDataMercadoPagoTerminalRepository extends JpaRepository<TerminalEntity, String> {
    Optional<TerminalEntity> findByPosIdAndOperationMode(String posId, String operationMode);
}
