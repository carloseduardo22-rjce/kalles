package dev.kalles.sale.mercadopago.adapter;

import com.google.gson.JsonObject;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.MPHttpClient;
import com.mercadopago.net.MPRequest;
import com.mercadopago.net.MPResponse;
import dev.kalles.sale.mercadopago.domain.Caixa;
import dev.kalles.sale.mercadopago.domain.Company;
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

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MercadoPagoPosAdapter — ACL: ERP Caixa → MP POS")
class MercadoPagoPosAdapterTest {

    @Mock
    private MPHttpClient httpClient;

    private MercadoPagoPosAdapter adapter;

    private static final String CAIXA_ID       = "CAIXA-ERP-001";
    private static final String CAIXA_NAME     = "Caixa 01";
    private static final java.util.UUID COMPANY_ID     = java.util.UUID.randomUUID();
    private static final Long   STORE_ID_MP    = 1234567L;
    private static final Long   POS_ID_MP      = 2711382L;

    private Company companyWithStore;
    private Company companyWithoutStore;
    private Caixa   caixaWithoutPos;
    private Caixa   caixaWithPos;

    @BeforeEach
    void setUp() {
        adapter = new MercadoPagoPosAdapter(httpClient, "mock-token");
        companyWithStore = new Company(
                COMPANY_ID, "Kalles Matriz", "Street", "1", "City", "ST", -23.0, -46.0, STORE_ID_MP
        );
        companyWithoutStore = new Company(
                java.util.UUID.randomUUID(), "Orphan Corp", "X", "1", "City", "ST", 0.0, 0.0, null
        );
        caixaWithoutPos = new Caixa(CAIXA_ID, CAIXA_NAME, COMPANY_ID.toString(), null);
        caixaWithPos = new Caixa(CAIXA_ID, CAIXA_NAME, COMPANY_ID.toString(), POS_ID_MP);
    }

    @Nested
    @DisplayName("Scenario 1 — Successful translation and creation")
    class SuccessfulCreation {

        @Test
        @DisplayName("Should invoke SDK with correct parameters")
        void shouldInvokeSdkWithCorrectParams() throws MPException, MPApiException {
            when(httpClient.send(any(MPRequest.class))).thenReturn(mockResponseWithId(POS_ID_MP));

            adapter.createPos(caixaWithoutPos, companyWithStore);

            ArgumentCaptor<MPRequest> captor = ArgumentCaptor.forClass(MPRequest.class);
            verify(httpClient).send(captor.capture());
            JsonObject payload = captor.getValue().getPayload();

            assertEquals(CAIXA_NAME, payload.get("name").getAsString());
            assertEquals(CAIXA_ID, payload.get("external_id").getAsString());
            assertEquals(STORE_ID_MP, payload.get("store_id").getAsLong());
            assertEquals(COMPANY_ID.toString(), payload.get("external_store_id").getAsString());
            assertFalse(payload.get("fixed_amount").getAsBoolean());
        }

        @Test
        @DisplayName("Should persist and return POS ID on success")
        void shouldPersistAndReturnPosId() throws MPException, MPApiException {
            when(httpClient.send(any(MPRequest.class))).thenReturn(mockResponseWithId(POS_ID_MP));

            Long result = adapter.createPos(caixaWithoutPos, companyWithStore);

            assertEquals(POS_ID_MP, result);
        }
    }

    @Nested
    @DisplayName("Scenario 2 — Idempotency: Caixa already has posId")
    class Idempotency {

        @Test
        @DisplayName("Should not invoke SDK if Caixa already has pos_id")
        void shouldNotInvokeSdkIfCaixaHasPosId() throws MPException, MPApiException {
            Long result = adapter.createPos(caixaWithPos, companyWithStore);

            verify(httpClient, never()).send(any(MPRequest.class));
            assertEquals(POS_ID_MP, result);
        }
    }

    @Nested
    @DisplayName("Scenario 3 — Precondition: Store not configured")
    class PreconditionStoreNotConfigured {

        @Test
        @DisplayName("Should throw IllegalStateException if Company lacks store_id")
        void shouldThrowExceptionIfNoStoreId() throws MPException, MPApiException {
            Caixa orphanCaixa = new Caixa("C999", "Orphan", "COMP-NO-STORE", null);

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> adapter.createPos(orphanCaixa, companyWithoutStore));

            assertTrue(ex.getMessage().toLowerCase().contains("store"));
            verify(httpClient, never()).send(any(MPRequest.class));
        }
    }

    @Nested
    @DisplayName("Scenario 4 — SDK Communication Failure")
    class CommunicationFailure {

        @Test
        @DisplayName("Should convert SDK exception into MercadoPagoIntegrationException")
        void shouldConvertSdkException() throws MPException, MPApiException {
            when(httpClient.send(any(MPRequest.class))).thenThrow(new MPException("Comm fail"));

            assertThrows(MercadoPagoIntegrationException.class,
                    () -> adapter.createPos(caixaWithoutPos, companyWithStore));
        }
    }

    private MPResponse mockResponseWithId(Long id) {
        return new MPResponse(201, Collections.emptyMap(), "{\"id\":" + id + "}");
    }
}
