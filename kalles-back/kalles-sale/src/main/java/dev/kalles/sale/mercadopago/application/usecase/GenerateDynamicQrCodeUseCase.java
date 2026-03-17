package dev.kalles.sale.mercadopago.application.usecase;

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

    public GenerateDynamicQrCodeUseCase(CaixaMpRepository caixaMpRepository, MercadoPagoOrderPort mercadoPagoOrderPort) {
        this.caixaMpRepository = caixaMpRepository;
        this.mercadoPagoOrderPort = mercadoPagoOrderPort;
    }

    public ResultadoQr execute(String pedidoIdErp, BigDecimal amount, String caixaId, String idempotencyKey) {
        Caixa caixa = caixaMpRepository.findById(caixaId)
                .orElseThrow(() -> new IllegalArgumentException("Caixa not found: " + caixaId));

        if (!caixa.hasPosRegistered()) {
            throw new IllegalStateException("Caixa does not have a Mercado Pago POS configured.");
        }

        String validIdempotencyKey = (idempotencyKey != null && !idempotencyKey.isBlank()) 
                ? idempotencyKey 
                : UUID.randomUUID().toString();

        CobrancaQr cobrancaQr = new CobrancaQr(pedidoIdErp, amount, caixaId, validIdempotencyKey);
        
        return mercadoPagoOrderPort.createOrder(cobrancaQr);
    }
}
