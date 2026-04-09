package dev.kalles.sale.mercadopago.adapter;

import dev.kalles.sale.support.LegacyMercadoPagoReferenceTest;
import dev.kalles.sale.mercadopago.domain.Company;
import dev.kalles.sale.mercadopago.exception.MercadoPagoIntegrationException;
import dev.kalles.sale.mercadopago.port.CompanyMpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import dev.kalles.sale.mercadopago.port.TenantRepository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MercadoPagoStoreAdapter — ACL: ERP Company to MP Store")
@LegacyMercadoPagoReferenceTest
class MercadoPagoStoreAdapterTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private CompanyMpRepository companyMpRepository;

    @Mock
    private TenantRepository tenantRepository;

    private MercadoPagoStoreAdapter adapter;

    private static final java.util.UUID COMPANY_ID    = java.util.UUID.randomUUID();
    private static final String COMPANY_NAME  = "Kalles Matriz";
    private static final String STREET_NAME   = "Rua das Flores";
    private static final String STREET_NUMBER = "123";
    private static final String CITY_NAME     = "São Paulo";
    private static final String STATE_NAME    = "SP";
    private static final double LATITUDE      = -23.550520;
    private static final double LONGITUDE     = -46.633308;
    private static final Long   STORE_ID_MP   = 1234567L;
    private static final String MP_USER_ID    = "test_user_id";

    private Company companyWithoutStore;
    private Company companyWithStore;
    private dev.kalles.sale.core.entity.Company coreCompany;

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient().when(tenantRepository.findById(org.mockito.ArgumentMatchers.any())).thenReturn(java.util.Optional.of(new dev.kalles.sale.mercadopago.domain.Tenant(java.util.UUID.randomUUID(), "mock-token", "test_user_id", "test_user_id", "test_user_id")));
        adapter = new MercadoPagoStoreAdapter(MP_USER_ID, "mock-token", httpClient, tenantRepository);
        coreCompany = new dev.kalles.sale.core.entity.Company(COMPANY_ID, COMPANY_NAME, java.util.UUID.randomUUID(), STREET_NAME, STREET_NUMBER, CITY_NAME, STATE_NAME,
                LATITUDE, LONGITUDE);

        companyWithoutStore = new Company(
                COMPANY_ID, java.util.UUID.randomUUID(), COMPANY_NAME, null
        );
        companyWithStore = new Company(
                COMPANY_ID, java.util.UUID.randomUUID(), COMPANY_NAME, STORE_ID_MP
        );
    }

    @Nested
    @DisplayName("Scenario 1 — Successful translation and creation")
    class SuccessfulCreation {

        @Test
        @DisplayName("Should invoke SDK with correct URI")
        void shouldInvokeSdkWithCorrectName() throws Exception {
            HttpResponse<String> mockResponse = mock(HttpResponse.class);
            when(mockResponse.statusCode()).thenReturn(201);
            when(mockResponse.body()).thenReturn("{\"id\":" + STORE_ID_MP + "}");
            when(httpClient.<String>send(any(HttpRequest.class), any())).thenReturn(mockResponse);

            adapter.createStore(companyWithoutStore, coreCompany);

            ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
            verify(httpClient, atLeastOnce()).send(captor.capture(), any());
            assertEquals(URI.create("https://api.mercadopago.com/users/" + MP_USER_ID + "/stores"), captor.getAllValues().get(captor.getAllValues().size()-1).uri());
        }

        @Test
        @DisplayName("Should return the correct storeId to the domain")
        void shouldReturnStoreIdToDomain() throws Exception {
            HttpResponse<String> mockResponse = mock(HttpResponse.class);
            when(mockResponse.statusCode()).thenReturn(201);
            when(mockResponse.body()).thenReturn("{\"id\":" + STORE_ID_MP + "}");
            when(httpClient.<String>send(any(HttpRequest.class), any())).thenReturn(mockResponse);

            Long result = adapter.createStore(companyWithoutStore, coreCompany);

            assertEquals(STORE_ID_MP, result);
        }
    }

    @Nested
    @DisplayName("Scenario 2 — Idempotency: Company already has storeId")
    class Idempotency {

        @Test
        @DisplayName("Should not invoke SDK if Company already has a registered store_id")
        void shouldNotInvokeSdkIfStoreIdExists() throws Exception {
            Long result = adapter.createStore(companyWithStore, coreCompany);

            verify(httpClient, never()).send(any(HttpRequest.class), any());
            assertEquals(STORE_ID_MP, result);
        }
    }

    @Nested
    @DisplayName("Scenario 3 — SDK Communication Failure")
    class CommunicationFailure {

        @Test
        @DisplayName("Should convert SDK exception into MercadoPagoIntegrationException")
        void shouldConvertSdkException() throws Exception {
            when(httpClient.<String>send(any(HttpRequest.class), any())).thenThrow(new java.io.IOException("Communication failure"));

            assertThrows(MercadoPagoIntegrationException.class,
                    () -> adapter.createStore(companyWithoutStore, coreCompany));
        }
    }
}
