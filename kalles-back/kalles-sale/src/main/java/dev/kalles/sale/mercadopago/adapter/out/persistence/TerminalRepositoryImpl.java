package dev.kalles.sale.mercadopago.adapter.out.persistence;

import dev.kalles.sale.mercadopago.adapter.out.persistence.entity.TerminalEntity;
import dev.kalles.sale.mercadopago.adapter.out.persistence.repository.SpringDataMercadoPagoTerminalRepository;
import dev.kalles.sale.mercadopago.domain.Terminal;
import dev.kalles.sale.mercadopago.port.TerminalRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TerminalRepositoryImpl implements TerminalRepository {

    private final SpringDataMercadoPagoTerminalRepository repository;

    public TerminalRepositoryImpl(SpringDataMercadoPagoTerminalRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Terminal> findById(String terminalId) {
        return repository.findById(terminalId).map(this::toDomain);
    }

    @Override
    public Optional<Terminal> findByPosIdAndOperationMode(String posId, String operationMode) {
        return repository.findByPosIdAndOperationMode(posId, operationMode).map(this::toDomain);
    }

    @Override
    public void save(Terminal terminal) {
        TerminalEntity entity = repository.findById(terminal.id()).orElse(new TerminalEntity());
        entity.setId(terminal.id());
        entity.setPosId(terminal.posId());
        entity.setStoreId(terminal.storeId());
        entity.setExternalPosId(terminal.externalPosId());
        entity.setOperationMode(terminal.operationMode());

        repository.save(entity);
    }

    @Override
    public void saveAll(List<Terminal> terminals) {
        List<TerminalEntity> entities = terminals.stream().map(this::toEntity).toList();
        repository.saveAll(entities);
    }

    private TerminalEntity toEntity(Terminal terminal) {
        TerminalEntity entity = new TerminalEntity();
        entity.setId(terminal.id());
        entity.setPosId(terminal.posId());
        entity.setStoreId(terminal.storeId());
        entity.setExternalPosId(terminal.externalPosId());
        entity.setOperationMode(terminal.operationMode());
        return entity;
    }

    private Terminal toDomain(TerminalEntity entity) {
        return new Terminal(
                entity.getId(),
                entity.getPosId(),
                entity.getStoreId(),
                entity.getExternalPosId(),
                entity.getOperationMode());
    }
}
