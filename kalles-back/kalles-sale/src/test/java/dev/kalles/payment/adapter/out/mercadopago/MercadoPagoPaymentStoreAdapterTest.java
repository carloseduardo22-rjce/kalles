package dev.kalles.payment.adapter.out.mercadopago;

import dev.kalles.payment.domain.MerchantProfile;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentStore;
import dev.kalles.payment.domain.PaymentStoreView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MercadoPagoPaymentStoreAdapterTest {

    private static final String USER_ID = "1234567890";
    private static final String STORES_URL = "https://api.mercadopago.com/users/" + USER_ID + "/stores";
    private static final String SEARCH_URL = STORES_URL + "/search";
    private static final String SEARCH_BY_EXTERNAL_ID_URL = SEARCH_URL + "?external_id=FILIAL-01";

    private static final MerchantProfile MERCHANT_PROFILE = new MerchantProfile(
            "Kalles Matriz",
            "Rua das Flores",
            "100",
            "Fortaleza",
            "CE",
            -3.73,
            -38.52
    );

    private MockRestServiceServer server;
    private MercadoPagoPaymentStoreAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        MercadoPagoCredentialsResolver credentialsResolver = mock(MercadoPagoCredentialsResolver.class);
        when(credentialsResolver.linkedAccessTokenOrThrow()).thenReturn("token-do-tenant");
        when(credentialsResolver.linkedUserIdOrThrow()).thenReturn(USER_ID);

        adapter = new MercadoPagoPaymentStoreAdapter(
                credentialsResolver,
                new MercadoPagoWebClient(builder),
                JsonMapper.builder().build()
        );
    }

    private PaymentStore newStore() {
        return new PaymentStore(UUID.randomUUID(), UUID.randomUUID(), PaymentProvider.MERCADO_PAGO, "FILIAL-01", null);
    }

    @Test
    void shouldReuseTheStoreAlreadyRegisteredAtMercadoPago() {
        server.expect(requestTo(SEARCH_BY_EXTERNAL_ID_URL))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer token-do-tenant"))
                .andRespond(withSuccess("""
                        {"results": [{"id": 555000111, "name": "Kalles Matriz", "external_id": "FILIAL-01"}]}
                        """, MediaType.APPLICATION_JSON));

        PaymentStore store = adapter.createStore(newStore(), MERCHANT_PROFILE);

        assertThat(store.providerStoreId()).isEqualTo("555000111");
        server.verify();
    }

    @Test
    void shouldCreateTheStoreWithTheMerchantLocation() {
        server.expect(requestTo(SEARCH_BY_EXTERNAL_ID_URL))
                .andRespond(withSuccess("{\"results\": []}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(STORES_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Content-Type", "application/json; charset=UTF-8"))
                .andExpect(jsonPath("$.name").value("Kalles Matriz"))
                .andExpect(jsonPath("$.external_id").value("FILIAL-01"))
                .andExpect(jsonPath("$.location.street_name").value("Rua das Flores"))
                .andExpect(jsonPath("$.location.street_number").value("100"))
                .andExpect(jsonPath("$.location.city_name").value("Fortaleza"))
                .andExpect(jsonPath("$.location.state_name").value("CE"))
                .andExpect(jsonPath("$.location.latitude").value(-3.73))
                .andExpect(jsonPath("$.location.longitude").value(-38.52))
                .andRespond(withSuccess("{\"id\": 555000222}", MediaType.APPLICATION_JSON));

        PaymentStore store = adapter.createStore(newStore(), MERCHANT_PROFILE);

        assertThat(store.providerStoreId()).isEqualTo("555000222");
        server.verify();
    }

    @Test
    void shouldFallBackToTheMerchantDefaultsWhenTheProfileIsEmpty() {
        server.expect(requestTo(SEARCH_BY_EXTERNAL_ID_URL))
                .andRespond(withSuccess("{\"results\": []}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(STORES_URL))
                .andExpect(jsonPath("$.name").value(""))
                .andExpect(jsonPath("$.location.street_name").value(""))
                .andExpect(jsonPath("$.location.street_number").value("S/N"))
                .andExpect(jsonPath("$.location.city_name").value("S/I"))
                .andExpect(jsonPath("$.location.state_name").value("S/I"))
                .andRespond(withSuccess("{\"id\": 555000333}", MediaType.APPLICATION_JSON));

        PaymentStore store = adapter.createStore(
                newStore(),
                new MerchantProfile(null, null, null, null, null, null, null)
        );

        assertThat(store.providerStoreId()).isEqualTo("555000333");
        server.verify();
    }

    @Test
    void shouldRecoverTheStoreIdWhenMercadoPagoSaysTheExternalIdIsAlreadyAssigned() {
        server.expect(requestTo(SEARCH_BY_EXTERNAL_ID_URL))
                .andRespond(withSuccess("{\"results\": []}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(STORES_URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"external_id is already assigned to this user\"}"));
        server.expect(requestTo(SEARCH_BY_EXTERNAL_ID_URL))
                .andRespond(withSuccess("{\"results\": [{\"id\": 555000444}]}", MediaType.APPLICATION_JSON));

        PaymentStore store = adapter.createStore(newStore(), MERCHANT_PROFILE);

        assertThat(store.providerStoreId()).isEqualTo("555000444");
        server.verify();
    }

    @Test
    void shouldFailWhenMercadoPagoRejectsTheStoreCreation() {
        server.expect(requestTo(SEARCH_BY_EXTERNAL_ID_URL))
                .andRespond(withSuccess("{\"results\": []}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(STORES_URL))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"internal error\"}"));

        assertThatThrownBy(() -> adapter.createStore(newStore(), MERCHANT_PROFILE))
                .isInstanceOf(MercadoPagoAdapterException.class)
                .hasMessageContaining("500");
        server.verify();
    }

    @Test
    void shouldKeepTheStoreUntouchedWhenItAlreadyHasAProviderId() {
        PaymentStore store = new PaymentStore(
                UUID.randomUUID(), UUID.randomUUID(), PaymentProvider.MERCADO_PAGO, "FILIAL-01", "555000555");

        assertThat(adapter.createStore(store, MERCHANT_PROFILE)).isSameAs(store);
        server.verify();
    }

    @Test
    void shouldListTheStores() {
        server.expect(requestTo(SEARCH_URL))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer token-do-tenant"))
                .andRespond(withSuccess("""
                        {
                          "results": [
                            {
                              "id": 555000111,
                              "name": "Kalles Matriz",
                              "external_id": "FILIAL-01",
                              "date_creation": "2026-01-15T10:00:00.000-03:00"
                            },
                            {
                              "id": 555000222,
                              "name": "Kalles Filial",
                              "external_id": "FILIAL-02",
                              "date_creation": "2026-02-20T11:30:00.000-03:00"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<PaymentStoreView> stores = adapter.listStores();

        assertThat(stores).hasSize(2);
        assertThat(stores.getFirst().providerStoreId()).isEqualTo("555000111");
        assertThat(stores.getFirst().name()).isEqualTo("Kalles Matriz");
        assertThat(stores.getFirst().externalReference()).isEqualTo("FILIAL-01");
        assertThat(stores.getFirst().createdAt()).isEqualTo("2026-01-15T10:00:00.000-03:00");
        assertThat(stores.getFirst().metadata()).isEmpty();
        assertThat(stores.getLast().providerStoreId()).isEqualTo("555000222");
        server.verify();
    }

    @Test
    void shouldListNoStoresWhenMercadoPagoFails() {
        server.expect(requestTo(SEARCH_URL))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThat(adapter.listStores()).isEmpty();
        server.verify();
    }
}
