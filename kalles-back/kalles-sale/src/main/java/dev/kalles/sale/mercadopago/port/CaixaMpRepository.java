package dev.kalles.sale.mercadopago.port;

import dev.kalles.sale.mercadopago.domain.Caixa;
import java.util.Optional;

public interface CaixaMpRepository {
    Optional<Caixa> findById(String caixaId);
    void save(Caixa caixa);
    void savePosId(String caixaId, Long posId);
}
