package dev.kalles.payment.adapter.out.mercadopago.persistence;

import dev.kalles.payment.adapter.out.mercadopago.persistence.entity.MercadoPagoTerminalEntity;
import dev.kalles.payment.adapter.out.mercadopago.persistence.repository.MercadoPagoTerminalJpaRepository;
import dev.kalles.payment.application.port.out.PaymentTerminalRepository;
import dev.kalles.payment.domain.PaymentTerminal;
import dev.kalles.payment.domain.TerminalOperationMode;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static dev.kalles.payment.adapter.out.mercadopago.MercadoPagoMappingUtils.toProviderOperationMode;
import static dev.kalles.payment.adapter.out.mercadopago.MercadoPagoMappingUtils.toTerminalOperationMode;

@Repository
public class MercadoPagoPaymentTerminalRepositoryAdapter implements PaymentTerminalRepository {

    private final MercadoPagoTerminalJpaRepository repository;

    public MercadoPagoPaymentTerminalRepositoryAdapter(MercadoPagoTerminalJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PaymentTerminal> findById(String terminalId) {
        return repository.findById(terminalId).map(this::toDomain);
    }

    @Override
    public Optional<PaymentTerminal> findByPointIdAndOperationMode(String pointId, TerminalOperationMode operationMode) {
        return repository.findByPointIdAndOperationMode(pointId, toProviderOperationMode(operationMode)).map(this::toDomain);
    }

    @Override
    public void saveAll(List<PaymentTerminal> terminals) {
        repository.saveAll(terminals.stream().map(this::toEntity).toList());
    }

    @Override
    public void save(PaymentTerminal terminal) {
        MercadoPagoTerminalEntity entity = repository.findById(terminal.id()).orElse(new MercadoPagoTerminalEntity());
        entity.setId(terminal.id());
        entity.setPointId(terminal.pointId());
        entity.setStoreId(terminal.storeId());
        entity.setExternalPointId(terminal.externalPointId());
        entity.setOperationMode(toProviderOperationMode(terminal.operationMode()));
        repository.save(entity);
    }

    private MercadoPagoTerminalEntity toEntity(PaymentTerminal terminal) {
        MercadoPagoTerminalEntity entity = new MercadoPagoTerminalEntity();
        entity.setId(terminal.id());
        entity.setPointId(terminal.pointId());
        entity.setStoreId(terminal.storeId());
        entity.setExternalPointId(terminal.externalPointId());
        entity.setOperationMode(toProviderOperationMode(terminal.operationMode()));
        return entity;
    }

    private PaymentTerminal toDomain(MercadoPagoTerminalEntity entity) {
        return new PaymentTerminal(
                entity.getId(),
                entity.getPointId(),
                entity.getStoreId(),
                entity.getExternalPointId(),
                toTerminalOperationMode(entity.getOperationMode())
        );
    }
}
