package dev.kalles.sale.mercadopago.adapter.out.persistence;

import dev.kalles.sale.mercadopago.adapter.out.persistence.repository.SpringDataMercadoPagoCaixaRepository;
import dev.kalles.sale.mercadopago.domain.Caixa;
import dev.kalles.sale.mercadopago.port.CaixaMpRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CaixaMpRepositoryImpl implements CaixaMpRepository {

    private final SpringDataMercadoPagoCaixaRepository repository;

    public CaixaMpRepositoryImpl(SpringDataMercadoPagoCaixaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Caixa> findById(String caixaId) {
        return repository.findById(caixaId)
                .map(entity -> new Caixa(
                        entity.getId(),
                        entity.getName(),
                        entity.getCompanyId(),
                        entity.getMpPosId()));
    }

    @Override
    public void save(Caixa caixa) {
        dev.kalles.sale.mercadopago.adapter.out.persistence.entity.MercadoPagoCaixaEntity entity = repository.findById(caixa.id())
                .orElse(new dev.kalles.sale.mercadopago.adapter.out.persistence.entity.MercadoPagoCaixaEntity());
        
        entity.setId(caixa.id());
        entity.setName(caixa.name());
        entity.setCompanyId(caixa.companyId());
        entity.setMpPosId(caixa.mpPosId());
        
        repository.save(entity);
    }

    @Override
    public void savePosId(String caixaId, Long posId) {
        repository.findById(caixaId).ifPresent(entity -> {
            entity.setMpPosId(posId);
            repository.save(entity);
        });
    }
}
