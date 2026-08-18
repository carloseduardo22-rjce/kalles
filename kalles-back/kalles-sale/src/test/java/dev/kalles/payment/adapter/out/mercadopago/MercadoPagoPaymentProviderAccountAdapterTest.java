package dev.kalles.payment.adapter.out.mercadopago;

import dev.kalles.payment.config.MercadoPagoProperties;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentProviderAuthorization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MercadoPagoPaymentProviderAccountAdapterTest {

    private static final String TOKEN_URL = "https://api.mercadopago.com/oauth/token";

    private MockRestServiceServer server;
    private MercadoPagoPaymentProviderAccountAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new MercadoPagoPaymentProviderAccountAdapter(
                new MercadoPagoProperties(
                        null,
                        null,
                        null,
                        "client-id-do-kalles",
                        "client-secret-do-kalles",
                        "https://kalles.dev/oauth/callback",
                        null
                ),
                new MercadoPagoWebClient(builder),
                JsonMapper.builder().build()
        );
    }

    @Test
    void shouldAnnounceMercadoPagoAsItsProvider() {
        assertThat(adapter.provider()).isEqualTo(PaymentProvider.MERCADO_PAGO);
    }

    @Test
    void shouldSendTheAuthorizationCodeAndReadEveryFieldOfTheAuthorization() {
        server.expect(requestTo(TOKEN_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Content-Type", "application/json; charset=UTF-8"))
                .andExpect(jsonPath("$.client_id").value("client-id-do-kalles"))
                .andExpect(jsonPath("$.client_secret").value("client-secret-do-kalles"))
                .andExpect(jsonPath("$.code").value("TG-authorization-code"))
                .andExpect(jsonPath("$.grant_type").value("authorization_code"))
                .andExpect(jsonPath("$.redirect_uri").value("https://kalles.dev/oauth/callback"))
                .andExpect(jsonPath("$.test_token").value("false"))
                .andRespond(withSuccess("""
                        {
                          "access_token": "APP_USR-access-token",
                          "token_type": "Bearer",
                          "expires_in": 15552000,
                          "scope": "offline_access read write",
                          "user_id": 1234567890,
                          "refresh_token": "TG-refresh-token",
                          "public_key": "APP_USR-public-key",
                          "live_mode": true,
                          "campo_que_o_mercado_pago_manda_e_o_kalles_ignora": "irrelevante"
                        }
                        """, MediaType.APPLICATION_JSON));

        PaymentProviderAuthorization authorization = adapter.exchangeAuthorizationCode("TG-authorization-code");

        assertThat(authorization.providerAccountId()).isEqualTo("1234567890");
        assertThat(authorization.accessToken()).isEqualTo("APP_USR-access-token");
        assertThat(authorization.refreshToken()).isEqualTo("TG-refresh-token");
        assertThat(authorization.tokenType()).isEqualTo("Bearer");
        assertThat(authorization.expiresIn()).isEqualTo(15552000L);
        assertThat(authorization.scope()).isEqualTo("offline_access read write");
        assertThat(authorization.publicKey()).isEqualTo("APP_USR-public-key");
        assertThat(authorization.liveMode()).isTrue();
        server.verify();
    }

    @Test
    void shouldLeaveTheOptionalFieldsNullWhenMercadoPagoOmitsThem() {
        server.expect(requestTo(TOKEN_URL))
                .andRespond(withSuccess("""
                        {
                          "access_token": "APP_USR-access-token",
                          "token_type": "Bearer",
                          "expires_in": 21600,
                          "user_id": 42,
                          "refresh_token": "TG-refresh-token"
                        }
                        """, MediaType.APPLICATION_JSON));

        PaymentProviderAuthorization authorization = adapter.exchangeAuthorizationCode("TG-authorization-code");

        assertThat(authorization.providerAccountId()).isEqualTo("42");
        assertThat(authorization.scope()).isNull();
        assertThat(authorization.publicKey()).isNull();
        assertThat(authorization.liveMode()).isNull();
        server.verify();
    }

    @Test
    void shouldFailWhenMercadoPagoRejectsTheAuthorizationCode() {
        server.expect(requestTo(TOKEN_URL))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"invalid_grant\"}"));

        assertThatThrownBy(() -> adapter.exchangeAuthorizationCode("TG-authorization-code"))
                .isInstanceOf(MercadoPagoAdapterException.class)
                .hasMessageContaining("401");
        server.verify();
    }
}
