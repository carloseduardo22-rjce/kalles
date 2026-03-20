package dev.kalles.sale.mercadopago.application.usecase;

import dev.kalles.sale.mercadopago.domain.Caixa;
import dev.kalles.sale.mercadopago.domain.Company;
import dev.kalles.sale.mercadopago.port.CaixaMpRepository;
import dev.kalles.sale.mercadopago.port.CompanyMpRepository;
import dev.kalles.sale.mercadopago.port.MercadoPagoPosPort;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class CreateMercadoPagoPosUseCase {

    private final CaixaMpRepository caixaMpRepository;
    private final CompanyMpRepository companyMpRepository;
    private final MercadoPagoPosPort mercadoPagoPosPort;

    public CreateMercadoPagoPosUseCase(CaixaMpRepository caixaMpRepository, CompanyMpRepository companyMpRepository,
            MercadoPagoPosPort mercadoPagoPosPort) {
        this.caixaMpRepository = caixaMpRepository;
        this.companyMpRepository = companyMpRepository;
        this.mercadoPagoPosPort = mercadoPagoPosPort;
    }

    public Long execute(Caixa caixaInfo) {
        Caixa caixa = caixaMpRepository.findByExternalId(caixaInfo.externalId())
                .orElseGet(() -> {
                    caixaMpRepository.save(caixaInfo);
                    return caixaInfo;
                });

        if (caixa.hasPosRegistered()) {
            return caixa.mpPosId(); // Idempotency
        }

        Company company = companyMpRepository.findByExternalId(caixa.companyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found for Caixa: " + caixa.externalId()));

        if (!company.hasStoreRegistered()) {
            throw new IllegalStateException("Company does not have a Mercado Pago Store configured.");
        }

        Long posId = mercadoPagoPosPort.createPos(caixa, company);
        caixaMpRepository.savePosId(caixa.externalId(), posId);

        return posId;
    }
}
