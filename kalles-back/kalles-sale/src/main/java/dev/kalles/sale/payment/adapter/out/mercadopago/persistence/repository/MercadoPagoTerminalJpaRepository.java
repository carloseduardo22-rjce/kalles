package dev.kalles.sale.payment.adapter.out.mercadopago.persistence.repository;

import dev.kalles.sale.payment.adapter.out.mercadopago.persistence.entity.MercadoPagoTerminalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MercadoPagoTerminalJpaRepository extends JpaRepository<MercadoPagoTerminalEntity, String> {

    Optional<MercadoPagoTerminalEntity> findByPointIdAndOperationMode(String pointId, String operationMode);
}
