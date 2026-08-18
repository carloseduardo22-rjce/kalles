package dev.kalles.payment.adapter.out.mercadopago;

import dev.kalles.payment.domain.PaymentTerminal;
import dev.kalles.payment.domain.TerminalOperationMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MercadoPagoPaymentTerminalAdapterTest {

    private static final String LIST_URL =
            "https://api.mercadopago.com/terminals/v1/list?limit=50&offset=0&store_id=STORE-01&pos_id=POS-01";
    private static final String SETUP_URL = "https://api.mercadopago.com/terminals/v1/setup";

    private MockRestServiceServer server;
    private MercadoPagoPaymentTerminalAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        MercadoPagoCredentialsResolver credentialsResolver = mock(MercadoPagoCredentialsResolver.class);
        when(credentialsResolver.linkedAccessTokenOrThrow()).thenReturn("token-do-tenant");

        adapter = new MercadoPagoPaymentTerminalAdapter(
                credentialsResolver,
                new MercadoPagoWebClient(builder),
                JsonMapper.builder().build()
        );
    }

    @Test
    void shouldListTheTerminalsAnsweredInSnakeCase() {
        server.expect(requestTo(LIST_URL))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer token-do-tenant"))
                .andRespond(withSuccess("""
                        {
                          "terminals": [
                            {
                              "id": "TERMINAL-01",
                              "pos_id": "POS-01",
                              "store_id": "STORE-01",
                              "external_pos_id": "CAIXA-01",
                              "operating_mode": "PDV"
                            },
                            {
                              "id": "TERMINAL-02",
                              "pos_id": "POS-02",
                              "store_id": "STORE-01",
                              "external_pos_id": "CAIXA-02",
                              "operating_mode": "STANDALONE"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<PaymentTerminal> terminals = adapter.listTerminals("STORE-01", "POS-01");

        assertThat(terminals).containsExactly(
                new PaymentTerminal("TERMINAL-01", "POS-01", "STORE-01", "CAIXA-01", TerminalOperationMode.POINT_OF_SALE),
                new PaymentTerminal("TERMINAL-02", "POS-02", "STORE-01", "CAIXA-02", TerminalOperationMode.STANDALONE)
        );
        server.verify();
    }

    @Test
    void shouldListTheTerminalsAnsweredInCamelCase() {
        server.expect(requestTo(LIST_URL))
                .andRespond(withSuccess("""
                        {
                          "terminals": [
                            {
                              "id": "TERMINAL-01",
                              "posId": "POS-01",
                              "storeId": "STORE-01",
                              "externalPosId": "CAIXA-01",
                              "operationMode": "STANDALONE"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<PaymentTerminal> terminals = adapter.listTerminals("STORE-01", "POS-01");

        assertThat(terminals).containsExactly(
                new PaymentTerminal("TERMINAL-01", "POS-01", "STORE-01", "CAIXA-01", TerminalOperationMode.STANDALONE)
        );
        server.verify();
    }

    @Test
    void shouldFallBackToUnknownWhenTheOperatingModeIsAbsent() {
        server.expect(requestTo(LIST_URL))
                .andRespond(withSuccess("""
                        {"terminals": [{"id": "TERMINAL-01", "pos_id": "POS-01"}]}
                        """, MediaType.APPLICATION_JSON));

        List<PaymentTerminal> terminals = adapter.listTerminals("STORE-01", "POS-01");

        assertThat(terminals).containsExactly(
                new PaymentTerminal("TERMINAL-01", "POS-01", null, null, TerminalOperationMode.UNKNOWN)
        );
        server.verify();
    }

    @Test
    void shouldReturnNoTerminalsWhenMercadoPagoAnswersAnEmptyList() {
        server.expect(requestTo(LIST_URL))
                .andRespond(withSuccess("{\"terminals\": []}", MediaType.APPLICATION_JSON));

        assertThat(adapter.listTerminals("STORE-01", "POS-01")).isEmpty();
        server.verify();
    }

    @Test
    void shouldReturnNoTerminalsWhenMercadoPagoFails() {
        server.expect(requestTo(LIST_URL))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThat(adapter.listTerminals("STORE-01", "POS-01")).isEmpty();
        server.verify();
    }

    @Test
    void shouldSendTheOperationModeChangeAndReportSuccess() {
        server.expect(requestTo(SETUP_URL))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("Authorization", "Bearer token-do-tenant"))
                .andExpect(header("Content-Type", "application/json"))
                .andExpect(jsonPath("$.terminals[0].id").value("TERMINAL-01"))
                .andExpect(jsonPath("$.terminals[0].operating_mode").value("STANDALONE"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        boolean changed = adapter.changeOperationMode("TERMINAL-01", TerminalOperationMode.STANDALONE);

        assertThat(changed).isTrue();
        server.verify();
    }

    @Test
    void shouldReportFailureWhenMercadoPagoRejectsTheOperationModeChange() {
        server.expect(requestTo(SETUP_URL))
                .andExpect(jsonPath("$.terminals[0].operating_mode").value("PDV"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        boolean changed = adapter.changeOperationMode("TERMINAL-01", TerminalOperationMode.POINT_OF_SALE);

        assertThat(changed).isFalse();
        server.verify();
    }
}
