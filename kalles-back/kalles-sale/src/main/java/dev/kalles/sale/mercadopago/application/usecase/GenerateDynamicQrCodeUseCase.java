package dev.kalles.sale.mercadopago.application.usecase;

import dev.kalles.sale.cashregister.entity.CashRegister;
import dev.kalles.sale.cashregister.repository.CashRegisterRepository;
import dev.kalles.sale.mercadopago.domain.Caixa;
import dev.kalles.sale.mercadopago.domain.CobrancaQr;
import dev.kalles.sale.mercadopago.domain.ResultadoQr;
import dev.kalles.sale.mercadopago.port.CaixaMpRepository;
import dev.kalles.sale.mercadopago.port.MercadoPagoOrderPort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class GenerateDynamicQrCodeUseCase {

    private final CaixaMpRepository caixaMpRepository;
    private final MercadoPagoOrderPort mercadoPagoOrderPort;
    private final CashRegisterRepository cashRegisterRepository;

    public GenerateDynamicQrCodeUseCase(CaixaMpRepository caixaMpRepository, MercadoPagoOrderPort mercadoPagoOrderPort,
            CashRegisterRepository cashRegisterRepository) {
        this.caixaMpRepository = caixaMpRepository;
        this.mercadoPagoOrderPort = mercadoPagoOrderPort;
        this.cashRegisterRepository = cashRegisterRepository;
    }

    public ResultadoQr execute(String pedidoIdErp, BigDecimal amount, String cashRegisterCode, String idempotencyKey) {
        CashRegister cashRegister = cashRegisterRepository.findByCode(cashRegisterCode)
                .orElseThrow(() -> new IllegalArgumentException("Cash Register code not found: " + cashRegisterCode));

        Caixa caixa = caixaMpRepository.findByExternalId(cashRegister.getId().toString())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Caixa Integration mapping not found for code: " + cashRegisterCode));

        if (!caixa.hasPosRegistered()) {
            throw new IllegalStateException("Caixa does not have a Mercado Pago POS configured.");
        }

        String validIdempotencyKey = (idempotencyKey != null && !idempotencyKey.isBlank())
                ? idempotencyKey
                : UUID.randomUUID().toString();

        CobrancaQr cobrancaQr = new CobrancaQr(pedidoIdErp, amount, caixa.externalId(), validIdempotencyKey);

        return mercadoPagoOrderPort.createOrder(cobrancaQr);
    }
}
