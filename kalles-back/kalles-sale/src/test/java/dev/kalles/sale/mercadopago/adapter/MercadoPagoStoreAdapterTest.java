package dev.kalles.sale.mercadopago.adapter;

import com.google.gson.JsonObject;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.MPHttpClient;
import com.mercadopago.net.MPRequest;
import com.mercadopago.net.MPResponse;
import dev.kalles.sale.mercadopago.domain.Company;
import dev.kalles.sale.mercadopago.exception.MercadoPagoIntegrationException;
import dev.kalles.sale.mercadopago.port.CompanyMpRepository;
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
@DisplayName("MercadoPagoStoreAdapter — ACL: ERP Company → MP Store")
class MercadoPagoStoreAdapterTest {

    @Mock
    private MPHttpClient httpClient;

    @Mock
    private CompanyMpRepository companyMpRepository;

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

    @BeforeEach
    void setUp() {
        adapter = new MercadoPagoStoreAdapter(httpClient, MP_USER_ID, "mock-token");

        companyWithoutStore = new Company(
                COMPANY_ID, COMPANY_NAME, STREET_NAME, STREET_NUMBER, CITY_NAME, STATE_NAME,
                LATITUDE, LONGITUDE, null
        );
        companyWithStore = new Company(
                COMPANY_ID, COMPANY_NAME, STREET_NAME, STREET_NUMBER, CITY_NAME, STATE_NAME,
                LATITUDE, LONGITUDE, STORE_ID_MP
        );
    }

    @Nested
    @DisplayName("Scenario 1 — Successful translation and creation")
    class SuccessfulCreation {

        @Test
        @DisplayName("Should invoke SDK with correct Company name")
        void shouldInvokeSdkWithCorrectName() throws MPException, MPApiException {
            when(httpClient.send(any(MPRequest.class))).thenReturn(mockResponseWithId(STORE_ID_MP));

            adapter.createStore(companyWithoutStore);

            ArgumentCaptor<MPRequest> captor = ArgumentCaptor.forClass(MPRequest.class);
            verify(httpClient).send(captor.capture());
            assertEquals(COMPANY_NAME, captor.getValue().getPayload().get("name").getAsString());
        }

        @Test
        @DisplayName("Should invoke SDK with external_id equal to Company ID")
        void shouldInvokeSdkWithCorrectExternalId() throws MPException, MPApiException {
            when(httpClient.send(any(MPRequest.class))).thenReturn(mockResponseWithId(STORE_ID_MP));

            adapter.createStore(companyWithoutStore);

            ArgumentCaptor<MPRequest> captor = ArgumentCaptor.forClass(MPRequest.class);
            verify(httpClient).send(captor.capture());
            assertEquals(COMPANY_ID.toString(), captor.getValue().getPayload().get("external_id").getAsString());
        }

        @Test
        @DisplayName("Should invoke SDK with correct location details")
        void shouldInvokeSdkWithCorrectLocation() throws MPException, MPApiException {
            when(httpClient.send(any(MPRequest.class))).thenReturn(mockResponseWithId(STORE_ID_MP));

            adapter.createStore(companyWithoutStore);

            ArgumentCaptor<MPRequest> captor = ArgumentCaptor.forClass(MPRequest.class);
            verify(httpClient).send(captor.capture());
            JsonObject location = captor.getValue().getPayload().getAsJsonObject("location");
            
            assertEquals(STREET_NAME, location.get("street_name").getAsString());
            assertEquals(STREET_NUMBER, location.get("street_number").getAsString());
            assertEquals(CITY_NAME, location.get("city_name").getAsString());
            assertEquals(STATE_NAME, location.get("state_name").getAsString());
            assertEquals(String.valueOf(LATITUDE), location.get("latitude").getAsString());
            assertEquals(String.valueOf(LONGITUDE), location.get("longitude").getAsString());
        }

        @Test
        @DisplayName("Should return the correct storeId to the domain")
        void shouldReturnStoreIdToDomain() throws MPException, MPApiException {
            when(httpClient.send(any(MPRequest.class))).thenReturn(mockResponseWithId(STORE_ID_MP));

            Long result = adapter.createStore(companyWithoutStore);

            assertEquals(STORE_ID_MP, result);
        }
    }

    @Nested
    @DisplayName("Scenario 2 — Idempotency: Company already has storeId")
    class Idempotency {

        @Test
        @DisplayName("Should not invoke SDK if Company already has a registered store_id")
        void shouldNotInvokeSdkIfStoreIdExists() throws MPException, MPApiException {
            Long result = adapter.createStore(companyWithStore);

            verify(httpClient, never()).send(any(MPRequest.class));
            assertEquals(STORE_ID_MP, result);
        }
    }

    @Nested
    @DisplayName("Scenario 3 — SDK Communication Failure")
    class CommunicationFailure {

        @Test
        @DisplayName("Should convert SDK exception into MercadoPagoIntegrationException")
        void shouldConvertSdkException() throws MPException, MPApiException {
            when(httpClient.send(any(MPRequest.class))).thenThrow(new MPException("Communication failure"));

            assertThrows(MercadoPagoIntegrationException.class,
                    () -> adapter.createStore(companyWithoutStore));
        }
    }

    private MPResponse mockResponseWithId(Long id) {
        return new MPResponse(201, Collections.emptyMap(), "{\"id\":" + id + "}");
    }
}
