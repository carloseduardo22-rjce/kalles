package dev.kalles.sale.payment.adapter.out.stone.persistence.repository;

import dev.kalles.sale.payment.adapter.out.stone.persistence.entity.StonePaymentOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StonePaymentOrderJpaRepository extends JpaRepository<StonePaymentOrderEntity, String> {
}
