package dev.kalles.sale.mercadopago.adapter.in.web;

import dev.kalles.sale.mercadopago.application.usecase.ProcessMercadoPagoWebhookUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mercadopago/webhook")
public class MercadoPagoWebhookController {

    private final ProcessMercadoPagoWebhookUseCase processMercadoPagoWebhookUseCase;
    private final String webhookSecret;

    public MercadoPagoWebhookController(
            ProcessMercadoPagoWebhookUseCase processMercadoPagoWebhookUseCase,
            @Value("${mercadopago.webhook-secret:}") String webhookSecret) {
        this.processMercadoPagoWebhookUseCase = processMercadoPagoWebhookUseCase;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping
    public ResponseEntity<Void> receiveNotification(
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId,
            @RequestParam(value = "data.id", required = false) String dataId,
            @RequestBody Map<String, Object> payload) {
        
        // Log para debug
        System.out.println("Webhook received! data.id: " + dataId);
        
        if (xSignature == null || xSignature.isBlank() || 
            xRequestId == null || xRequestId.isBlank() || 
            dataId == null || dataId.isBlank()) {
            System.err.println("Campos obrigatorios ausentes. Ignorando validacao ou bloqueando.");
            // Algumas notificações podem não trazer a assinatura no sandbox, mas seguindo a documentação:
            // return ResponseEntity.badRequest().build();
        }

        if (!webhookSecret.isBlank() && xSignature != null && xRequestId != null && dataId != null) {
            boolean isValid = processMercadoPagoWebhookUseCase.validateSignature(xSignature, xRequestId, dataId);
            if (!isValid) {
                System.err.println("Assinatura invalida para notificação: " + dataId);
                return ResponseEntity.status(403).build(); // Forbidden
            }
        }

        // Passa adiante para o UseCase (processar evento order.processed, etc)
        processMercadoPagoWebhookUseCase.execute(payload);

        return ResponseEntity.ok().build();
    }
}
