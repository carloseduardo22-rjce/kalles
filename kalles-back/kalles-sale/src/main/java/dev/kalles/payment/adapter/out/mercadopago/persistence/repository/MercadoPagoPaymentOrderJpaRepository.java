package dev.kalles.payment.adapter.out.mercadopago.persistence.repository;

import dev.kalles.payment.adapter.out.mercadopago.persistence.entity.MercadoPagoPaymentOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MercadoPagoPaymentOrderJpaRepository extends JpaRepository<MercadoPagoPaymentOrderEntity, String> {
}
