package dev.kalles.sale.mercadopago.adapter;

import com.google.gson.JsonObject;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.MPHttpClient;
import com.mercadopago.net.MPRequest;
import com.mercadopago.net.MPResponse;
import dev.kalles.sale.mercadopago.domain.Caixa;
import dev.kalles.sale.mercadopago.domain.CobrancaQr;
import dev.kalles.sale.mercadopago.domain.ResultadoQr;
import dev.kalles.sale.mercadopago.exception.MercadoPagoIntegrationException;
import dev.kalles.sale.mercadopago.port.CaixaMpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MercadoPagoOrderAdapter — ACL: ERP CobrancaQr → MP Order (Dynamic QR)")
class MercadoPagoOrderAdapterTest {

    @Mock
    private MPHttpClient httpClient;

    @Mock
    private CaixaMpRepository caixaMpRepository;

    @InjectMocks
    private MercadoPagoOrderAdapter adapter;

    private static final String ORDER_ID_ERP     = "PEDIDO-ERP-9999";
    private static final BigDecimal AMOUNT       = new BigDecimal("50.00");
    private static final String CAIXA_EXT_ID     = "CAIXA-ERP-001";
    private static final String IDEMPOTENCY_KEY  = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final Long   POS_ID_MP        = 2711382L;
    private static final String ORDER_ID_MP      = "ORD01K372G4J4FXZ9HGHZMJMGGPKE";
    private static final String QR_DATA_EMVCO    = "00020101021226580014br.gov.bcb.qr0136...";

    private CobrancaQr validCobranca;
    private Caixa caixaWithPos;

    @BeforeEach
    void setUp() {
        validCobranca = new CobrancaQr(ORDER_ID_ERP, AMOUNT, CAIXA_EXT_ID, IDEMPOTENCY_KEY);
        caixaWithPos = new Caixa(java.util.UUID.randomUUID(), CAIXA_EXT_ID, "Caixa 01", "COMP-001", POS_ID_MP);
    }

    @Nested
    @DisplayName("Scenario 1 — Successful dynamic QR generation")
    class SuccessfulQrGeneration {

        @BeforeEach
        void setupMocks() throws MPException, MPApiException {
            when(caixaMpRepository.findByExternalId(CAIXA_EXT_ID))
                    .thenReturn(Optional.of(caixaWithPos));
            
            MPResponse returnedResponse = mockOrderWithSuccess(ORDER_ID_MP, QR_DATA_EMVCO);
            when(httpClient.send(any(MPRequest.class))).thenReturn(returnedResponse);
        }

        @Test
        @DisplayName("Should invoke SDK with correct request body parameters")
        void shouldInvokeSdkWithCorrectRequest() throws MPException, MPApiException {
            adapter.createOrder(validCobranca);

            ArgumentCaptor<MPRequest> captor = ArgumentCaptor.forClass(MPRequest.class);
            verify(httpClient).send(captor.capture());
            JsonObject payload = captor.getValue().getPayload();
            
            assertEquals("qr", payload.get("type").getAsString());
            assertEquals(AMOUNT.doubleValue(), payload.get("total_amount").getAsDouble());
            assertEquals(ORDER_ID_ERP, payload.get("external_reference").getAsString());
            assertEquals(CAIXA_EXT_ID, payload.getAsJsonObject("config").getAsJsonObject("qr").get("external_pos_id").getAsString());
            assertEquals("dynamic", payload.getAsJsonObject("config").getAsJsonObject("qr").get("mode").getAsString());
            assertEquals(AMOUNT.doubleValue(), payload.getAsJsonObject("transactions").getAsJsonArray("payments").get(0).getAsJsonObject().get("amount").getAsDouble());
        }

        @Test
        @DisplayName("Should pass X-Idempotency-Key in SDK options")
        void shouldPassIdempotencyKey() throws MPException, MPApiException {
            adapter.createOrder(validCobranca);

            ArgumentCaptor<MPRequest> captor = ArgumentCaptor.forClass(MPRequest.class);
            verify(httpClient).send(captor.capture());
            
            assertEquals(IDEMPOTENCY_KEY, captor.getValue().getHeaders().get("X-Idempotency-Key"));
        }

        @Test
        @DisplayName("Should return clean ResultadoQr to domain")
        void shouldReturnCleanResultToDomain() throws MPException, MPApiException {
            ResultadoQr result = adapter.createOrder(validCobranca);

            assertEquals(ORDER_ID_MP, result.orderId());
            assertEquals(QR_DATA_EMVCO, result.qrData());
        }
    }

    @Nested
    @DisplayName("Scenario 2 — Mode always dynamic (Business Invariant)")
    class ModeInvariant {

        @Test
        @DisplayName("Mode must be 'dynamic' regardless of inputs")
        void modeMustAlwaysBeDynamic() throws MPException, MPApiException {
            CobrancaQr anotherCobranca = new CobrancaQr("P-111", new BigDecimal("100"), CAIXA_EXT_ID, "uuid");
            when(caixaMpRepository.findByExternalId(CAIXA_EXT_ID)).thenReturn(Optional.of(caixaWithPos));
            when(httpClient.send(any(MPRequest.class))).thenReturn(mockOrderWithSuccess("O-id", "qr-data"));

            adapter.createOrder(anotherCobranca);

            ArgumentCaptor<MPRequest> captor = ArgumentCaptor.forClass(MPRequest.class);
            verify(httpClient).send(captor.capture());
            JsonObject payload = captor.getValue().getPayload();
            
            assertEquals("dynamic", payload.getAsJsonObject("config").getAsJsonObject("qr").get("mode").getAsString());
        }
    }

    @Nested
    @DisplayName("Scenario 3 — Precondition: Caixa has no posId")
    class PreconditionCaixaNoPos {

        @Test
        @DisplayName("Should throw exception if Caixa lacks pos_id")
        void shouldThrowExceptionIfCaixaHasNoPosId() throws MPException, MPApiException {
            Caixa caixaSemPos = new Caixa(java.util.UUID.randomUUID(), CAIXA_EXT_ID, "No POS", "COMP-001", null);
            when(caixaMpRepository.findByExternalId(CAIXA_EXT_ID)).thenReturn(Optional.of(caixaSemPos));

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> adapter.createOrder(validCobranca));

            assertTrue(ex.getMessage().toLowerCase().contains("pos"));
            verify(httpClient, never()).send(any(MPRequest.class));
        }
    }

    @Nested
    @DisplayName("Scenario 4 — Failure: SDK returns Order without qr_data")
    class FailureNoQrData {

        @Test
        @DisplayName("Should throw exception when qr_data is missing from SDK response")
        void shouldThrowExceptionWhenQrDataMissing() throws MPException, MPApiException {
            when(caixaMpRepository.findByExternalId(CAIXA_EXT_ID)).thenReturn(Optional.of(caixaWithPos));
            when(httpClient.send(any(MPRequest.class))).thenReturn(mockOrderWithoutQrData(ORDER_ID_MP));

            MercadoPagoIntegrationException ex = assertThrows(
                    MercadoPagoIntegrationException.class,
                    () -> adapter.createOrder(validCobranca)
            );

            assertTrue(ex.getMessage().toLowerCase().contains("qr_data"));
        }
    }

    @Nested
    @DisplayName("Scenario 5 — SDK Communication Failure")
    class CommunicationFailure {

        @Test
        @DisplayName("Should convert SDK exception")
        void shouldConvertSdkException() throws MPException, MPApiException {
            when(caixaMpRepository.findByExternalId(CAIXA_EXT_ID)).thenReturn(Optional.of(caixaWithPos));
            when(httpClient.send(any(MPRequest.class))).thenThrow(new MPException("Comm error"));

            assertThrows(MercadoPagoIntegrationException.class,
                    () -> adapter.createOrder(validCobranca));
        }
    }

    private MPResponse mockOrderWithSuccess(String orderId, String qrData) {
        String json = "{\"id\":\"" + orderId + "\",\"type_response\":{\"qr_data\":\"" + qrData + "\"}}";
        return new MPResponse(201, Collections.emptyMap(), json);
    }

    private MPResponse mockOrderWithoutQrData(String orderId) {
        String json = "{\"id\":\"" + orderId + "\",\"type_response\":{}}";
        return new MPResponse(201, Collections.emptyMap(), json);
    }
}
