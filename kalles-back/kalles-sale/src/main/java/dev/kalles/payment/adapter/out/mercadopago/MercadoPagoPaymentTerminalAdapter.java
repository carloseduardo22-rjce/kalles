package dev.kalles.payment.adapter.out.mercadopago;

import dev.kalles.payment.adapter.out.mercadopago.dto.TerminalListResponse;
import dev.kalles.payment.adapter.out.mercadopago.dto.TerminalSetupRequest;
import dev.kalles.payment.application.port.out.PaymentTerminalPort;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentTerminal;
import dev.kalles.payment.domain.TerminalOperationMode;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static dev.kalles.payment.adapter.out.mercadopago.MercadoPagoMappingUtils.toProviderOperationMode;
import static dev.kalles.payment.adapter.out.mercadopago.MercadoPagoMappingUtils.toTerminalOperationMode;

@Component
public class MercadoPagoPaymentTerminalAdapter implements PaymentTerminalPort {

    private final MercadoPagoCredentialsResolver credentialsResolver;
    private final MercadoPagoWebClient mercadoPagoWebClient;
    private final ObjectMapper objectMapper;

    public MercadoPagoPaymentTerminalAdapter(
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
    public List<PaymentTerminal> listTerminals(String storeId, String pointId) {
        String token = credentialsResolver.linkedAccessTokenOrThrow();

        try {
            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.GET,
                    "https://api.mercadopago.com/terminals/v1/list?limit=50&offset=0&store_id="
                            + URLEncoder.encode(storeId, StandardCharsets.UTF_8)
                            + "&pos_id="
                            + URLEncoder.encode(pointId, StandardCharsets.UTF_8),
                    null,
                    Map.of("Authorization", "Bearer " + token)
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                return List.of();
            }

            TerminalListResponse listResponse = objectMapper.readValue(response.getBody(), TerminalListResponse.class);
            if (listResponse.terminals() == null || listResponse.terminals().isEmpty()) {
                return List.of();
            }

            return listResponse.terminals().stream()
                    .map(terminal -> new PaymentTerminal(
                            terminal.id(),
                            terminal.posId(),
                            terminal.storeId(),
                            terminal.externalPosId(),
                            toTerminalOperationMode(terminal.operatingMode())
                    ))
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public boolean changeOperationMode(String terminalId, TerminalOperationMode operationMode) {
        String token = credentialsResolver.linkedAccessTokenOrThrow();

        try {
            TerminalSetupRequest payload = new TerminalSetupRequest(List.of(
                    new TerminalSetupRequest.Terminal(terminalId, toProviderOperationMode(operationMode))
            ));

            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.PATCH,
                    "https://api.mercadopago.com/terminals/v1/setup",
                    objectMapper.writeValueAsString(payload),
                    Map.of(
                            "Authorization", "Bearer " + token,
                            "Content-Type", "application/json"
                    )
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}
