package dev.kalles.payment.adapter.out.mercadopago;

import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.repository.CashRegisterRepository;
import dev.kalles.payment.adapter.out.mercadopago.dto.OrderResponse;
import dev.kalles.payment.adapter.out.mercadopago.dto.OrderTransactionsRequest;
import dev.kalles.payment.adapter.out.mercadopago.dto.PointOrderRequest;
import dev.kalles.payment.adapter.out.mercadopago.dto.QrOrderRequest;
import dev.kalles.payment.application.port.out.PaymentGatewayPort;
import dev.kalles.payment.application.port.out.PaymentPointRepository;
import dev.kalles.payment.domain.PaymentCommand;
import dev.kalles.payment.domain.PaymentDocumentPrintCommand;
import dev.kalles.payment.domain.PaymentFlow;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentResult;
import dev.kalles.payment.domain.PaymentStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

import static dev.kalles.payment.adapter.out.mercadopago.MercadoPagoMappingUtils.toPaymentStatus;
import static dev.kalles.payment.adapter.out.mercadopago.MercadoPagoMappingUtils.toProviderPaymentMethodType;

@Component
public class MercadoPagoPaymentGatewayAdapter implements PaymentGatewayPort {

    private final MercadoPagoCredentialsResolver credentialsResolver;
    private final MercadoPagoWebClient mercadoPagoWebClient;
    private final CashRegisterRepository cashRegisterRepository;
    private final PaymentPointRepository paymentPointRepository;
    private final ObjectMapper objectMapper;

    public MercadoPagoPaymentGatewayAdapter(
            MercadoPagoCredentialsResolver credentialsResolver,
            MercadoPagoWebClient mercadoPagoWebClient,
            CashRegisterRepository cashRegisterRepository,
            PaymentPointRepository paymentPointRepository,
            ObjectMapper objectMapper
    ) {
        this.credentialsResolver = credentialsResolver;
        this.mercadoPagoWebClient = mercadoPagoWebClient;
        this.cashRegisterRepository = cashRegisterRepository;
        this.paymentPointRepository = paymentPointRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentProvider provider() {
        return PaymentProvider.MERCADO_PAGO;
    }

    @Override
    public PaymentResult processPayment(PaymentCommand command) {
        return switch (command.flow()) {
            case QR_CODE -> createQrOrder(command);
            case TERMINAL -> createPointOrder(command);
        };
    }

    @Override
    public PaymentResult getPayment(String providerOrderId) {
        try {
            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.GET,
                    "https://api.mercadopago.com/v1/orders/" + providerOrderId,
                    null,
                    authorizationHeaders(credentialsResolver.fallbackAccessToken())
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new MercadoPagoAdapterException("Fail to get Point order. HTTP Status: "
                        + response.getStatusCode().value() + " - " + response.getBody());
            }

            return toOrderResult(objectMapper.readValue(response.getBody(), OrderResponse.class));
        } catch (MercadoPagoAdapterException e) {
            throw e;
        } catch (Exception e) {
            throw new MercadoPagoAdapterException("Fail to get Point order: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancelPayment(String providerOrderId) {
        try {
            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.POST,
                    "https://api.mercadopago.com/v1/orders/" + providerOrderId + "/cancel",
                    null,
                    jsonAuthorizationHeaders(credentialsResolver.fallbackAccessToken())
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new MercadoPagoAdapterException("Fail to cancel Point Order. HTTP Status: "
                        + response.getStatusCode().value() + " - " + response.getBody());
            }
        } catch (MercadoPagoAdapterException e) {
            throw e;
        } catch (Exception e) {
            throw new MercadoPagoAdapterException("Fail to cancel Point order: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentResult closePaymentOrder(String providerOrderId, PaymentStatus status) {
        if (status != PaymentStatus.CANCELED) {
            throw new IllegalArgumentException("Mercado Pago only supports canceling orders through this operation");
        }
        cancelPayment(providerOrderId);
        return getPayment(providerOrderId);
    }

    @Override
    public void printDocument(String providerOrderId, PaymentDocumentPrintCommand command) {
        throw new IllegalStateException("Mercado Pago does not support document printing through this integration");
    }

    @Override
    public void refundPayment(String providerPaymentId) {
        try {
            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.POST,
                    "https://api.mercadopago.com/v1/payments/" + providerPaymentId + "/refunds",
                    null,
                    jsonAuthorizationHeaders(credentialsResolver.fallbackAccessToken())
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new MercadoPagoAdapterException("Fail to refund Point payment. HTTP Status: "
                        + response.getStatusCode().value() + " - " + response.getBody());
            }
        } catch (MercadoPagoAdapterException e) {
            throw e;
        } catch (Exception e) {
            throw new MercadoPagoAdapterException("Fail to refund Point payment: " + e.getMessage(), e);
        }
    }

    private PaymentResult createQrOrder(PaymentCommand command) {
        CashRegister cashRegister = cashRegisterRepository.findByCode(command.targetId())
                .orElseThrow(() -> new IllegalArgumentException("Cash Register code not found: " + command.targetId()));

        var point = paymentPointRepository.findByCashRegisterIdAndProvider(cashRegister.getId(), PaymentProvider.MERCADO_PAGO)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Caixa Integration mapping not found for code: " + command.targetId()));

        if (!point.hasProviderPoint()) {
            throw new IllegalStateException("Caixa does not have a Mercado Pago POS configured.");
        }

        QrOrderRequest payload = new QrOrderRequest(
                "qr",
                command.amount().toPlainString(),
                command.externalReference(),
                new QrOrderRequest.Config(new QrOrderRequest.Qr(point.externalReference(), "dynamic")),
                OrderTransactionsRequest.ofSinglePayment(command.amount().toPlainString())
        );

        try {
            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.POST,
                    "https://api.mercadopago.com/v1/orders",
                    objectMapper.writeValueAsString(payload),
                    idempotentJsonAuthorizationHeaders(credentialsResolver.fallbackAccessToken(), command.idempotencyKey())
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new MercadoPagoAdapterException("Fail to create MP Order. HTTP Status: "
                        + response.getStatusCode().value() + " - " + response.getBody());
            }

            OrderResponse order = objectMapper.readValue(response.getBody(), OrderResponse.class);
            if (order.id() == null || order.typeResponse() == null) {
                throw new MercadoPagoAdapterException("SDK returned Order without ID or type_response");
            }

            if (order.typeResponse().qrData() == null) {
                throw new MercadoPagoAdapterException("SDK returned Order without qr_data");
            }

            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("qrData", order.typeResponse().qrData());

            return new PaymentResult(
                    order.id(),
                    null,
                    PaymentStatus.CREATED,
                    metadata
            );
        } catch (MercadoPagoAdapterException e) {
            throw e;
        } catch (Exception e) {
            throw new MercadoPagoAdapterException("Fail to generate dynamic QR: " + e.getMessage(), e);
        }
    }

    private PaymentResult createPointOrder(PaymentCommand command) {
        PointOrderRequest payload = new PointOrderRequest(
                "point",
                command.externalReference(),
                command.description(),
                new PointOrderRequest.Config(
                        new PointOrderRequest.Point(command.targetId(), "no_ticket"),
                        new PointOrderRequest.PaymentMethod(toProviderPaymentMethodType(command.methodType()))
                ),
                OrderTransactionsRequest.ofSinglePayment(command.amount().toPlainString())
        );

        try {
            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.POST,
                    "https://api.mercadopago.com/v1/orders",
                    objectMapper.writeValueAsString(payload),
                    idempotentJsonAuthorizationHeaders(credentialsResolver.fallbackAccessToken(), command.idempotencyKey())
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new MercadoPagoAdapterException("Fail to create Point Order. HTTP Status: "
                        + response.getStatusCode().value() + " - " + response.getBody());
            }

            return toOrderResult(objectMapper.readValue(response.getBody(), OrderResponse.class));
        } catch (MercadoPagoAdapterException e) {
            throw e;
        } catch (Exception e) {
            throw new MercadoPagoAdapterException("Fail to create Point order: " + e.getMessage(), e);
        }
    }

    private PaymentResult toOrderResult(OrderResponse order) {
        return new PaymentResult(
                order.id(),
                order.firstPaymentId(),
                toPaymentStatus(order.status()),
                Map.of()
        );
    }

    private Map<String, String> authorizationHeaders(String accessToken) {
        return Map.of("Authorization", "Bearer " + accessToken);
    }

    private Map<String, String> jsonAuthorizationHeaders(String accessToken) {
        return Map.of(
                "Authorization", "Bearer " + accessToken,
                "Content-Type", "application/json; charset=UTF-8"
        );
    }

    private Map<String, String> idempotentJsonAuthorizationHeaders(String accessToken, String idempotencyKey) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        headers.put("X-Idempotency-Key", idempotencyKey);
        headers.put("Content-Type", "application/json; charset=UTF-8");
        return headers;
    }
}
