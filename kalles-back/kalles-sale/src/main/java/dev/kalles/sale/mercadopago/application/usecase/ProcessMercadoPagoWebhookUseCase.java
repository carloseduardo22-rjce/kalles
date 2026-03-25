package dev.kalles.sale.mercadopago.application.usecase;

import dev.kalles.sale.core.enums.payment.PaymentMethod;
import dev.kalles.sale.core.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class ProcessMercadoPagoWebhookUseCase {

    private final PaymentService paymentService;
    private final String webhookSecret;

    public ProcessMercadoPagoWebhookUseCase(
            PaymentService paymentService,
            @Value("${mercadopago.webhook-secret:}") String webhookSecret) {
        this.paymentService = paymentService;
        this.webhookSecret = webhookSecret;
    }

    public boolean validateSignature(String xSignature, String xRequestId, String dataId) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            return false;
        }

        try {
            String[] parts = xSignature.split(",");
            String ts = null;
            String hash = null;

            for (String part : parts) {
                String[] keyValue = part.split("=", 2);
                if (keyValue.length == 2) {
                    if ("ts".equals(keyValue[0].trim())) {
                        ts = keyValue[1].trim();
                    } else if ("v1".equals(keyValue[0].trim())) {
                        hash = keyValue[1].trim();
                    }
                }
            }

            if (ts == null || hash == null) {
                return false;
            }

            String lowerDataId = dataId.toLowerCase();
            String manifest = String.format("id:%s;request-id:%s;ts:%s;", lowerDataId, xRequestId, ts);

            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKey);
            byte[] hmacBytes = hmac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));

            String calculatedHash = bytesToHex(hmacBytes);
            
            return calculatedHash.equalsIgnoreCase(hash);
        } catch (Exception e) {
            System.err.println("Erro ao validar assinatura HMAC: " + e.getMessage());
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public void execute(Map<String, Object> payload) {
        System.out.println("Processing Mercado Pago Webhook: " + payload);

        String action = (String) payload.get("action");
        if ("order.processed".equals(action)) {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            if (data != null) {
                String externalReference = (String) data.get("external_reference");
                String status = (String) data.get("status");
                
                Object amountObj = data.get("total_paid_amount");
                BigDecimal amount = BigDecimal.ZERO;
                if (amountObj != null) {
                    amount = new BigDecimal(amountObj.toString());
                }

                if ("processed".equals(status) || "closed".equals(status)) {
                    if (externalReference != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                        try {
                            paymentService.addPayment(externalReference, PaymentMethod.PIX, amount);
                            System.out.println("Pagamento confirmado para sessionToken/pedido ERP: " + externalReference);
                        } catch (Exception e) {
                            System.err.println("Erro ao processar o pagamento da venda: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
