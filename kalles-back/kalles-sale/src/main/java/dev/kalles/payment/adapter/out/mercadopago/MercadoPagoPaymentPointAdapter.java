package dev.kalles.payment.adapter.out.mercadopago;

import dev.kalles.payment.adapter.out.mercadopago.dto.PointCreateRequest;
import dev.kalles.payment.adapter.out.mercadopago.dto.PointResponse;
import dev.kalles.payment.adapter.out.mercadopago.dto.PointSearchResponse;
import dev.kalles.payment.application.port.out.PaymentPointPort;
import dev.kalles.payment.domain.PaymentPoint;
import dev.kalles.payment.domain.PaymentPointDescriptor;
import dev.kalles.payment.domain.PaymentPointView;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentStore;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class MercadoPagoPaymentPointAdapter implements PaymentPointPort {

    private final MercadoPagoCredentialsResolver credentialsResolver;
    private final MercadoPagoWebClient mercadoPagoWebClient;
    private final ObjectMapper objectMapper;

    public MercadoPagoPaymentPointAdapter(
            MercadoPagoCredentialsResolver credentialsResolver,
            MercadoPagoWebClient mercadoPagoWebClient,
            ObjectMapper objectMapper
    ) {
        this.credentialsResolver = credentialsResolver;
        this.mercadoPagoWebClient = mercadoPagoWebClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentProvider provider() {
        return PaymentProvider.MERCADO_PAGO;
    }

    @Override
    public PaymentPoint createPoint(PaymentPoint point, PaymentStore store, PaymentPointDescriptor descriptor) {
        String token = credentialsResolver.linkedAccessTokenOrThrow();

        if (point.hasProviderPoint()) {
            return point;
        }

        if (!store.hasProviderStore()) {
            throw new IllegalStateException("Company does not have a Mercado Pago Store configured.");
        }

        String extId = point.externalReference() != null ? point.externalReference().trim() : "";
        String existingPosId = searchPosId(extId, token);
        if (existingPosId != null) {
            return point.withProviderPointId(existingPosId);
        }

        PointCreateRequest payload = new PointCreateRequest(
                descriptor.description() + " - " + descriptor.code(),
                false,
                Long.valueOf(store.providerStoreId()),
                store.externalReference(),
                extId
        );

        try {
            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.POST,
                    "https://api.mercadopago.com/pos",
                    objectMapper.writeValueAsString(payload),
                    Map.of(
                            "Authorization", "Bearer " + token,
                            "Content-Type", "application/json; charset=UTF-8"
                    )
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                if (responseBody != null && responseBody.contains("is already assigned")) {
                    String fallbackPosId = searchPosId(extId, token);
                    if (fallbackPosId != null) {
                        return point.withProviderPointId(fallbackPosId);
                    }
                }
                throw new MercadoPagoAdapterException("Fail to create MP POS. HTTP Status: "
                        + response.getStatusCode().value() + " - " + responseBody);
            }

            PointResponse createdPoint = objectMapper.readValue(response.getBody(), PointResponse.class);
            if (createdPoint.id() == null) {
                throw new MercadoPagoAdapterException("SDK returned POS without id");
            }

            return point.withProviderPointId(createdPoint.id());
        } catch (MercadoPagoAdapterException e) {
            throw e;
        } catch (Exception e) {
            throw new MercadoPagoAdapterException("Fail to create MP POS: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PaymentPointView> listPoints() {
        String token = credentialsResolver.linkedAccessTokenOrThrow();

        try {
            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.GET,
                    "https://api.mercadopago.com/pos",
                    null,
                    Map.of("Authorization", "Bearer " + token)
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                return List.of();
            }

            PointSearchResponse searchResponse = objectMapper.readValue(response.getBody(), PointSearchResponse.class);
            if (!searchResponse.hasResults()) {
                return List.of();
            }

            return searchResponse.results().stream()
                    .map(point -> new PaymentPointView(
                            point.id(),
                            point.name(),
                            point.storeId(),
                            point.externalId(),
                            point.externalStoreId(),
                            point.fixedAmount(),
                            point.status(),
                            point.dateCreated(),
                            point.dateLastUpdated(),
                            Map.of()
                    ))
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private String searchPosId(String externalId, String token) {
        try {
            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.GET,
                    "https://api.mercadopago.com/pos?external_id=" + URLEncoder.encode(externalId, StandardCharsets.UTF_8),
                    null,
                    Map.of("Authorization", "Bearer " + token)
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                return null;
            }

            PointSearchResponse searchResponse = objectMapper.readValue(response.getBody(), PointSearchResponse.class);
            if (searchResponse.hasResults()) {
                return searchResponse.results().getFirst().id();
            }
            return searchResponse.id();
        } catch (Exception e) {
            return null;
        }
    }
}
