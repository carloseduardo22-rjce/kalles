package dev.kalles.payment.adapter.out.mercadopago;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MercadoPagoWebClientTest {

    private static final String POS_URL = "https://api.mercadopago.com/pos";

    private MockRestServiceServer server;
    private MercadoPagoWebClient mercadoPagoWebClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        mercadoPagoWebClient = new MercadoPagoWebClient(builder);
    }

    @Test
    void shouldSendTheBodyAndHeadersAndReturnTheSuccessfulResponse() {
        server.expect(requestTo(POS_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer token-do-tenant"))
                .andExpect(header("Content-Type", "application/json; charset=UTF-8"))
                .andExpect(content().string("{\"external_id\":\"CAIXA-01\"}"))
                .andRespond(withSuccess("{\"id\":123}", MediaType.APPLICATION_JSON));

        ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                HttpMethod.POST,
                POS_URL,
                "{\"external_id\":\"CAIXA-01\"}",
                Map.of(
                        "Authorization", "Bearer token-do-tenant",
                        "Content-Type", "application/json; charset=UTF-8"
                )
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("{\"id\":123}");
        server.verify();
    }

    @Test
    void shouldReturnTheErrorResponseWithItsBodyInsteadOfThrowing() {
        server.expect(requestTo(POS_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"external_id is already assigned\"}"));

        ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                HttpMethod.POST,
                POS_URL,
                "{\"external_id\":\"CAIXA-01\"}",
                Map.of("Content-Type", "application/json; charset=UTF-8")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("is already assigned");
        server.verify();
    }

    @Test
    void shouldReturnTheNotFoundResponseInsteadOfThrowing() {
        server.expect(requestTo(POS_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        ResponseEntity<String> response = mercadoPagoWebClient.exchange(HttpMethod.GET, POS_URL, null, Map.of());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        server.verify();
    }

    @Test
    void shouldReturnAResponseWithoutBody() {
        server.expect(requestTo(POS_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                HttpMethod.GET,
                POS_URL,
                null,
                Map.of("Authorization", "Bearer token-do-tenant")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        server.verify();
    }

    @Test
    void shouldKeepTheQueryStringUntouched() {
        String searchUrl = POS_URL + "?external_id=CAIXA%2F01";
        server.expect(requestTo(searchUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"paging\":{\"total\":0}}", MediaType.APPLICATION_JSON));

        ResponseEntity<String> response = mercadoPagoWebClient.exchange(HttpMethod.GET, searchUrl, null, Map.of());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }
}
