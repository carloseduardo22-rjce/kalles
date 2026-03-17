package dev.kalles.sale.mercadopago.application.usecase;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProcessMercadoPagoWebhookUseCase {

    public void execute(Map<String, Object> payload) {
        // Inicialmente logamos o payload para debug, depois podemos integrar para baixar o pagamento e dar baixa no ERP.
        System.out.println("Received Mercado Pago Webhook: " + payload);
    }
}
