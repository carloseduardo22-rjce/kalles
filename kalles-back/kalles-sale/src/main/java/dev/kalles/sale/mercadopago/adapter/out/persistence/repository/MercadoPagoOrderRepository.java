package dev.kalles.sale.mercadopago.adapter.out.persistence.repository;

import dev.kalles.sale.mercadopago.adapter.out.persistence.entity.MercadoPagoOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MercadoPagoOrderRepository extends JpaRepository<MercadoPagoOrderEntity, String> {
}
