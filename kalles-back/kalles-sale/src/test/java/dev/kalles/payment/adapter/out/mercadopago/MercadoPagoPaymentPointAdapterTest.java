package dev.kalles.payment.adapter.out.mercadopago;

import dev.kalles.payment.domain.PaymentPoint;
import dev.kalles.payment.domain.PaymentPointDescriptor;
import dev.kalles.payment.domain.PaymentPointView;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentStore;
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

class MercadoPagoPaymentPointAdapterTest {

    private static final String POS_URL = "https://api.mercadopago.com/pos";
    private static final String SEARCH_URL = POS_URL + "?external_id=CAIXA-01";

    private static final PaymentPointDescriptor DESCRIPTOR = new PaymentPointDescriptor("Caixa da frente", "CAIXA-01");

    private MockRestServiceServer server;
    private MercadoPagoPaymentPointAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        MercadoPagoCredentialsResolver credentialsResolver = mock(MercadoPagoCredentialsResolver.class);
        when(credentialsResolver.linkedAccessTokenOrThrow()).thenReturn("token-do-tenant");

        adapter = new MercadoPagoPaymentPointAdapter(
                credentialsResolver,
                new MercadoPagoWebClient(builder),
                JsonMapper.builder().build()
        );
    }

    private PaymentPoint newPoint() {
        return new PaymentPoint(UUID.randomUUID(), UUID.randomUUID(), PaymentProvider.MERCADO_PAGO, "CAIXA-01", null);
    }

    private PaymentStore storeWithProviderId() {
        return new PaymentStore(
                UUID.randomUUID(), UUID.randomUUID(), PaymentProvider.MERCADO_PAGO, "FILIAL-01", "555000111");
    }

    @Test
    void shouldReuseThePointAlreadyRegisteredAtMercadoPago() {
        server.expect(requestTo(SEARCH_URL))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer token-do-tenant"))
                .andRespond(withSuccess("""
                        {"paging": {"total": 1}, "results": [{"id": 777000111}]}
                        """, MediaType.APPLICATION_JSON));

        PaymentPoint point = adapter.createPoint(newPoint(), storeWithProviderId(), DESCRIPTOR);

        assertThat(point.providerPointId()).isEqualTo("777000111");
        server.verify();
    }

    @Test
    void shouldReadTheTopLevelIdWhenTheSearchAnswersASinglePoint() {
        server.expect(requestTo(SEARCH_URL))
                .andRespond(withSuccess("{\"id\": 777000222}", MediaType.APPLICATION_JSON));

        PaymentPoint point = adapter.createPoint(newPoint(), storeWithProviderId(), DESCRIPTOR);

        assertThat(point.providerPointId()).isEqualTo("777000222");
        server.verify();
    }

    @Test
    void shouldCreateThePointLinkedToTheStore() {
        server.expect(requestTo(SEARCH_URL))
                .andRespond(withSuccess("{\"paging\": {\"total\": 0}, \"results\": []}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(POS_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Content-Type", "application/json; charset=UTF-8"))
                .andExpect(jsonPath("$.name").value("Caixa da frente - CAIXA-01"))
                .andExpect(jsonPath("$.fixed_amount").value(false))
                .andExpect(jsonPath("$.store_id").value(555000111))
                .andExpect(jsonPath("$.external_store_id").value("FILIAL-01"))
                .andExpect(jsonPath("$.external_id").value("CAIXA-01"))
                .andRespond(withSuccess("{\"id\": 777000333}", MediaType.APPLICATION_JSON));

        PaymentPoint point = adapter.createPoint(newPoint(), storeWithProviderId(), DESCRIPTOR);

        assertThat(point.providerPointId()).isEqualTo("777000333");
        server.verify();
    }

    @Test
    void shouldRecoverThePointIdWhenMercadoPagoSaysTheExternalIdIsAlreadyAssigned() {
        server.expect(requestTo(SEARCH_URL))
                .andRespond(withSuccess("{\"paging\": {\"total\": 0}, \"results\": []}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(POS_URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"external_id is already assigned\"}"));
        server.expect(requestTo(SEARCH_URL))
                .andRespond(withSuccess("""
                        {"paging": {"total": 1}, "results": [{"id": 777000444}]}
                        """, MediaType.APPLICATION_JSON));

        PaymentPoint point = adapter.createPoint(newPoint(), storeWithProviderId(), DESCRIPTOR);

        assertThat(point.providerPointId()).isEqualTo("777000444");
        server.verify();
    }

    @Test
    void shouldFailWhenMercadoPagoRejectsThePointCreation() {
        server.expect(requestTo(SEARCH_URL))
                .andRespond(withSuccess("{\"paging\": {\"total\": 0}, \"results\": []}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(POS_URL))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"internal error\"}"));

        assertThatThrownBy(() -> adapter.createPoint(newPoint(), storeWithProviderId(), DESCRIPTOR))
                .isInstanceOf(MercadoPagoAdapterException.class)
                .hasMessageContaining("500");
        server.verify();
    }

    @Test
    void shouldFailWhenTheStoreHasNoProviderStore() {
        PaymentStore storeWithoutProvider = new PaymentStore(
                UUID.randomUUID(), UUID.randomUUID(), PaymentProvider.MERCADO_PAGO, "FILIAL-01", null);

        assertThatThrownBy(() -> adapter.createPoint(newPoint(), storeWithoutProvider, DESCRIPTOR))
                .isInstanceOf(IllegalStateException.class);
        server.verify();
    }

    @Test
    void shouldKeepThePointUntouchedWhenItAlreadyHasAProviderId() {
        PaymentPoint point = new PaymentPoint(
                UUID.randomUUID(), UUID.randomUUID(), PaymentProvider.MERCADO_PAGO, "CAIXA-01", "777000555");

        assertThat(adapter.createPoint(point, storeWithProviderId(), DESCRIPTOR)).isSameAs(point);
        server.verify();
    }

    @Test
    void shouldListThePoints() {
        server.expect(requestTo(POS_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "paging": {"total": 2},
                          "results": [
                            {
                              "id": 777000111,
                              "name": "Caixa 1",
                              "store_id": 555000111,
                              "external_id": "CAIXA-01",
                              "external_store_id": "FILIAL-01",
                              "fixed_amount": false,
                              "status": "active",
                              "date_created": "2026-01-15T10:00:00.000-03:00",
                              "date_last_updated": "2026-02-10T09:00:00.000-03:00"
                            },
                            {
                              "id": 777000222,
                              "name": "Caixa 2",
                              "store_id": 555000111,
                              "external_id": "CAIXA-02",
                              "external_store_id": "FILIAL-01",
                              "fixed_amount": true,
                              "status": "active",
                              "date_created": "2026-01-16T10:00:00.000-03:00",
                              "date_last_updated": "2026-02-11T09:00:00.000-03:00"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<PaymentPointView> points = adapter.listPoints();

        assertThat(points).hasSize(2);
        PaymentPointView first = points.getFirst();
        assertThat(first.providerPointId()).isEqualTo("777000111");
        assertThat(first.name()).isEqualTo("Caixa 1");
        assertThat(first.providerStoreId()).isEqualTo("555000111");
        assertThat(first.externalReference()).isEqualTo("CAIXA-01");
        assertThat(first.externalStoreReference()).isEqualTo("FILIAL-01");
        assertThat(first.fixedAmount()).isFalse();
        assertThat(first.status()).isEqualTo("active");
        assertThat(first.createdAt()).isEqualTo("2026-01-15T10:00:00.000-03:00");
        assertThat(first.updatedAt()).isEqualTo("2026-02-10T09:00:00.000-03:00");
        assertThat(first.metadata()).isEmpty();
        assertThat(points.getLast().fixedAmount()).isTrue();
        server.verify();
    }

    @Test
    void shouldListNoPointsWhenThePagingIsEmpty() {
        server.expect(requestTo(POS_URL))
                .andRespond(withSuccess("{\"paging\": {\"total\": 0}, \"results\": []}", MediaType.APPLICATION_JSON));

        assertThat(adapter.listPoints()).isEmpty();
        server.verify();
    }

    @Test
    void shouldListNoPointsWhenMercadoPagoFails() {
        server.expect(requestTo(POS_URL))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThat(adapter.listPoints()).isEmpty();
        server.verify();
    }
}
