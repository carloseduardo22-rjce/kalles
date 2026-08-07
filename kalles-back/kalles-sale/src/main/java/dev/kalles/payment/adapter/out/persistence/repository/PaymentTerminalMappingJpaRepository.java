package dev.kalles.payment.adapter.out.persistence.repository;

import dev.kalles.payment.adapter.out.persistence.entity.PaymentTerminalMappingEntity;
import dev.kalles.payment.domain.PaymentProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentTerminalMappingJpaRepository extends JpaRepository<PaymentTerminalMappingEntity, UUID> {

    Optional<PaymentTerminalMappingEntity> findByCashRegisterIdAndProviderAndActiveTrue(
            UUID cashRegisterId,
            PaymentProvider provider
    );

    Optional<PaymentTerminalMappingEntity> findByCompanyIdAndProviderAndTerminalSerialAndActiveTrue(
            UUID companyId,
            PaymentProvider provider,
            String terminalSerial
    );

    List<PaymentTerminalMappingEntity> findAllByCompanyIdAndProviderAndActiveTrueOrderByUpdatedAtDesc(
            UUID companyId,
            PaymentProvider provider
    );
}
