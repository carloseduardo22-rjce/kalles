package dev.kalles.sale.mercadopago.adapter;

import dev.kalles.sale.mercadopago.domain.Caixa;
import dev.kalles.sale.mercadopago.domain.Company;
import dev.kalles.sale.mercadopago.exception.MercadoPagoIntegrationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MercadoPagoPosAdapter — ACL: ERP Caixa to MP POS")
class MercadoPagoPosAdapterTest {

    @Mock
    private HttpClient httpClient;

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
                COMPANY_ID, "EXT-LOJ001", "Kalles Matriz", "Street", "1", "City", "ST", -23.0, -46.0, STORE_ID_MP
        );
        companyWithoutStore = new Company(
                java.util.UUID.randomUUID(), "EXT-LOJ002", "Orphan Corp", "X", "1", "City", "ST", 0.0, 0.0, null
        );
        caixaWithoutPos = new Caixa(java.util.UUID.randomUUID(), CAIXA_ID, CAIXA_NAME, "EXT-LOJ001", null);
        caixaWithPos = new Caixa(java.util.UUID.randomUUID(), CAIXA_ID, CAIXA_NAME, "EXT-LOJ001", POS_ID_MP);
    }

    @Nested
    @DisplayName("Scenario 1 — Successful translation and creation")
    class SuccessfulCreation {

        @Test
        @DisplayName("Should invoke SDK with correct URI")
        void shouldInvokeSdkWithCorrectParams() throws Exception {
            HttpResponse<String> mockResponseSearch = mock(HttpResponse.class);
            when(mockResponseSearch.statusCode()).thenReturn(404);
            HttpResponse<String> mockResponse = mock(HttpResponse.class);
            when(mockResponse.statusCode()).thenReturn(201);
            when(mockResponse.body()).thenReturn("{\"id\":" + POS_ID_MP + "}");
            when(httpClient.<String>send(any(HttpRequest.class), any())).thenReturn(mockResponseSearch, mockResponse);

            adapter.createPos(caixaWithoutPos, companyWithStore);

            ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
            verify(httpClient, atLeastOnce()).send(captor.capture(), any());
            assertEquals(URI.create("https://api.mercadopago.com/pos"), captor.getAllValues().get(captor.getAllValues().size()-1).uri());
        }

        @Test
        @DisplayName("Should persist and return POS ID on success")
        void shouldPersistAndReturnPosId() throws Exception {
            HttpResponse<String> mockResponseSearch = mock(HttpResponse.class);
            when(mockResponseSearch.statusCode()).thenReturn(404);
            HttpResponse<String> mockResponse = mock(HttpResponse.class);
            when(mockResponse.statusCode()).thenReturn(201);
            when(mockResponse.body()).thenReturn("{\"id\":" + POS_ID_MP + "}");
            when(httpClient.<String>send(any(HttpRequest.class), any())).thenReturn(mockResponseSearch, mockResponse);

            Long result = adapter.createPos(caixaWithoutPos, companyWithStore);

            assertEquals(POS_ID_MP, result);
        }
    }

    @Nested
    @DisplayName("Scenario 2 — Idempotency: Caixa already has posId")
    class Idempotency {

        @Test
        @DisplayName("Should not invoke SDK if Caixa already has pos_id")
        void shouldNotInvokeSdkIfCaixaHasPosId() throws Exception {
            Long result = adapter.createPos(caixaWithPos, companyWithStore);

            verify(httpClient, never()).send(any(HttpRequest.class), any());
            assertEquals(POS_ID_MP, result);
        }
    }

    @Nested
    @DisplayName("Scenario 3 — Precondition: Store not configured")
    class PreconditionStoreNotConfigured {

        @Test
        @DisplayName("Should throw IllegalStateException if Company lacks store_id")
        void shouldThrowExceptionIfNoStoreId() throws Exception {
            Caixa orphanCaixa = new Caixa(java.util.UUID.randomUUID(), "C999", "Orphan", "COMP-NO-STORE", null);
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> adapter.createPos(orphanCaixa, companyWithoutStore));
            assertTrue(ex.getMessage().toLowerCase().contains("store"));
            verify(httpClient, never()).send(any(HttpRequest.class), any());
        }
    }

    @Nested
    @DisplayName("Scenario 4 — SDK Communication Failure")
    class CommunicationFailure {

        @Test
        @DisplayName("Should convert SDK exception into MercadoPagoIntegrationException")
        void shouldConvertSdkException() throws Exception {
            HttpResponse<String> mockResponseSearch = mock(HttpResponse.class);
            when(mockResponseSearch.statusCode()).thenReturn(404);
            when(httpClient.<String>send(any(HttpRequest.class), any()))
                .thenReturn(mockResponseSearch)
                .thenThrow(new java.io.IOException("Comm fail"));

            assertThrows(MercadoPagoIntegrationException.class,
                    () -> adapter.createPos(caixaWithoutPos, companyWithStore));
        }
    }
}
