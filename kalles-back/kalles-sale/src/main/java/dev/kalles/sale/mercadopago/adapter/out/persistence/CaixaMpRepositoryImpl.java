package dev.kalles.sale.mercadopago.adapter.out.persistence;

import dev.kalles.sale.mercadopago.adapter.out.persistence.entity.MercadoPagoCaixaEntity;
import dev.kalles.sale.mercadopago.adapter.out.persistence.repository.SpringDataMercadoPagoCaixaRepository;
import dev.kalles.sale.mercadopago.domain.Caixa;
import dev.kalles.sale.mercadopago.port.CaixaMpRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class CaixaMpRepositoryImpl implements CaixaMpRepository {

    private final SpringDataMercadoPagoCaixaRepository repository;

    public CaixaMpRepositoryImpl(SpringDataMercadoPagoCaixaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Caixa> findById(UUID id) {
        return repository.findById(id)
                .map(entity -> new Caixa(
                        entity.getId(),
                        entity.getExternalId(),
                        entity.getCashRegisterId(),
                        entity.getMpPosId()));
    }

    @Override
    public Optional<Caixa> findByExternalId(String externalId) {
        return repository.findByExternalId(externalId)
                .map(entity -> new Caixa(
                        entity.getId(),
                        entity.getExternalId(),
                        entity.getCashRegisterId(),
                        entity.getMpPosId()));
    }

    @Override
    public void save(Caixa caixa) {
        MercadoPagoCaixaEntity entity = null;
        if (caixa.id() != null) {
            entity = repository.findById(caixa.id()).orElse(new MercadoPagoCaixaEntity());
        } else if (caixa.externalId() != null) {
            entity = repository.findByExternalId(caixa.externalId()).orElse(new MercadoPagoCaixaEntity());
        } else {
            entity = new MercadoPagoCaixaEntity();
        }
        
        entity.setId(caixa.id());
        entity.setExternalId(caixa.externalId());
        entity.setCashRegisterId(caixa.cashRegisterId());
        entity.setMpPosId(caixa.mpPosId());
        
        repository.save(entity);
    }

    @Override
    public void savePosId(String externalId, Long posId) {
        repository.findByExternalId(externalId).ifPresent(entity -> {
            entity.setMpPosId(posId);
            repository.save(entity);
        });
    }
}
