package dev.kalles.sale.mercadopago.port;

import dev.kalles.sale.mercadopago.domain.Caixa;
import java.util.Optional;
import java.util.UUID;

public interface CaixaMpRepository {
    Optional<Caixa> findById(UUID id);
    Optional<Caixa> findByExternalId(String externalId);
    void save(Caixa caixa);
    void savePosId(String externalId, Long posId);
}
