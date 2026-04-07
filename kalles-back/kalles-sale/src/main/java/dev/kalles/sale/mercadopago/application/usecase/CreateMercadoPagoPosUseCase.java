package dev.kalles.sale.mercadopago.application.usecase;

import dev.kalles.sale.cashregister.entity.CashRegister;
import dev.kalles.sale.cashregister.repository.CashRegisterRepository;
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
    private final CashRegisterRepository cashRegisterRepository;

    public CreateMercadoPagoPosUseCase(CaixaMpRepository caixaMpRepository, CompanyMpRepository companyMpRepository,
            MercadoPagoPosPort mercadoPagoPosPort, CashRegisterRepository cashRegisterRepository) {
        this.caixaMpRepository = caixaMpRepository;
        this.companyMpRepository = companyMpRepository;
        this.mercadoPagoPosPort = mercadoPagoPosPort;
        this.cashRegisterRepository = cashRegisterRepository;
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

        CashRegister cashRegister = cashRegisterRepository.findById(caixa.cashRegisterId())
                .orElseThrow(() -> new IllegalArgumentException("CashRegister not found for Caixa: " + caixa.cashRegisterId()));

        Company company = companyMpRepository.findById(cashRegister.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Company mapping not found for Core Company: " + cashRegister.getCompanyId()));

        if (!company.hasStoreRegistered()) {
            throw new IllegalStateException("Company does not have a Mercado Pago Store configured.");
        }

        Long posId = mercadoPagoPosPort.createPos(caixa, company, cashRegister);
        caixaMpRepository.savePosId(caixa.externalId(), posId);

        return posId;
    }
}
