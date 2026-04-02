package dev.kalles.sale.mercadopago.application.usecase;

import dev.kalles.sale.core.enums.payment.PaymentMethod;
import dev.kalles.sale.core.service.PaymentService;
import dev.kalles.sale.mercadopago.domain.PointOrder;
import dev.kalles.sale.mercadopago.domain.PointOrderStatus;
import dev.kalles.sale.mercadopago.port.PointOrderPersistencePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class ProcessMercadoPagoWebhookUseCase {

    private final PaymentService paymentService;
    private final PointOrderPersistencePort pointOrderPersistencePort;
    private final String webhookSecret;

    public ProcessMercadoPagoWebhookUseCase(
            PaymentService paymentService,
            PointOrderPersistencePort pointOrderPersistencePort,
            @Value("${mercadopago.webhook-secret:}") String webhookSecret) {
        this.paymentService = paymentService;
        this.pointOrderPersistencePort = pointOrderPersistencePort;
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

    @Transactional
    @SuppressWarnings("unchecked")
    public void execute(Map<String, Object> payload) {
        System.out.println("Processing Mercado Pago Webhook: " + payload);

        String action = (String) payload.get("action");
        if (action == null) return;

        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        if (data == null) return;

        String type = (String) data.get("type");
        
        // Verifica se é uma notificação do Mercado Pago Point
        if ("point".equals(type)) {
            processPointOrderNotification(action, data);
        } else {
            // Processamento padrão para QR Code e integrações legadas
            if ("order.processed".equals(action)) {
                processLegacyPixNotification(data);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processPointOrderNotification(String action, Map<String, Object> data) {
        String externalReference = (String) data.get("external_reference");
        String status = (String) data.get("status");
        String orderId = (String) data.get("id");

        if (orderId != null) {
            pointOrderPersistencePort.findByOrderId(orderId).ifPresent(order -> {
                // Atualiza o status conforme notificação (processed, canceled, etc)
                try {
                    order.setStatus(PointOrderStatus.valueOf(status.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    System.err.println("Status de order desconhecido: " + status);
                }
                
                // Se houver um pagamento ID no payload processado, atualiza
                Map<String, Object> transactions = (Map<String, Object>) data.get("transactions");
                String methodStr = "PIX";
                if (transactions != null) {
                    List<Map<String, Object>> payments = (List<Map<String, Object>>) transactions.get("payments");
                    if (payments != null && !payments.isEmpty()) {
                        Map<String, Object> payment = payments.get(0);
                        String paymentId = (String) payment.get("id");
                        if (paymentId != null) {
                            order.setPaymentId(paymentId);
                        }
                        
                        Map<String, Object> paymentMethod = (Map<String, Object>) payment.get("payment_method");
                        if (paymentMethod != null) {
                            String pType = (String) paymentMethod.get("type");
                            if ("credit_card".equals(pType)) methodStr = "CREDIT_CARD";
                            else if ("debit_card".equals(pType)) methodStr = "DEBIT_CARD";
                            else if ("voucher_card".equals(pType)) methodStr = "CREDIT_CARD"; // ou equivalente
                        }
                    }
                }
                pointOrderPersistencePort.save(order);

                // Executar a ação final no ERP se for aprovada
                if ("order.processed".equals(action) && ("processed".equals(status) || "closed".equals(status))) {
                    Object amountObj = data.get("total_paid_amount");
                    if (amountObj != null) {
                        BigDecimal amount = new BigDecimal(amountObj.toString());
                        if (externalReference != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                            try {
                                PaymentMethod method = PaymentMethod.valueOf(methodStr);
                                paymentService.addPayment(externalReference, method, amount);
                                System.out.println("Pagamento POS (" + methodStr + ") confirmado na order " + orderId);
                            } catch (Exception e) {
                                System.err.println("Erro ao processar o pagamento POINT da venda: " + e.getMessage());
                            }
                        }
                    }
                }
                
                if ("order.canceled".equals(action) || "order.failed".equals(action) || "order.expired".equals(action)) {
                    System.out.println("Pedido de pagamento POS falhou ou foi cancelado: " + orderId);
                    // O ERP Kalles permite retentar em caso de cancelamento sem cancelar a venda inteira automaticamente.
                }
            });
        }
    }

    private void processLegacyPixNotification(Map<String, Object> data) {
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
