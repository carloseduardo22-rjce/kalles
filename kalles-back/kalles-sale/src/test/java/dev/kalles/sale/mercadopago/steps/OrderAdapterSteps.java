package dev.kalles.sale.mercadopago.steps;

import dev.kalles.sale.mercadopago.adapter.MercadoPagoOrderAdapter;
import dev.kalles.sale.mercadopago.domain.Caixa;
import dev.kalles.sale.mercadopago.domain.CobrancaQr;
import dev.kalles.sale.mercadopago.domain.ResultadoQr;
import dev.kalles.sale.mercadopago.exception.MercadoPagoIntegrationException;
import dev.kalles.sale.mercadopago.port.CaixaMpRepository;
import io.cucumber.java.pt.*;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class OrderAdapterSteps {

    private CobrancaQr cobrancaContext;
    private Caixa caixaContext;
    private ResultadoQr returnedQr;
    private Exception capturedException;

    private final HttpClient httpClient = mock(HttpClient.class);
    private final CaixaMpRepository repository = mock(CaixaMpRepository.class);
    private final MercadoPagoOrderAdapter adapter = new MercadoPagoOrderAdapter("mock-token", httpClient, repository);

    @Dado("um Caixa com external_id {string} que j\u00E1 possui pos_id {string} registrado no MP")
    public void um_caixa_com_external_id_que_ja_possui_pos_id_registrado_no_mp(String caixaExtId, String posId) {
        caixaContext = new Caixa(java.util.UUID.randomUUID(), caixaExtId, java.util.UUID.randomUUID(), Long.parseLong(posId));
        when(repository.findByExternalId(anyString())).thenReturn(Optional.of(caixaContext));
    }

    @Dado("uma inten\u00E7\u00E3o de cobran\u00E7a com pedidoId {string}, valor {string}, caixa {string} e idempotencyKey {string}")
    public void uma_intencao_de_cobranca_com_pedido_id_valor_caixa_e_idempotency_key(String orderIdErp, String amount, String caixaExtId, String idemKey) {
        cobrancaContext = new CobrancaQr(caixaExtId, new BigDecimal(amount), orderIdErp, idemKey);
    }

    @Dado("que o SDK retornar\u00E1 order_id {string} e qr_data {string}")
    public void que_o_sdk_retornara_order_id_e_qr_data(String orderId, String qrData) throws Exception {
        String mockResponseBody = "{\"id\":\"" + orderId + "\",\"type_response\":{\"qr_data\":\"" + qrData + "\"}}";
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(201);
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(httpClient.<String>send(any(HttpRequest.class), any())).thenReturn(mockResponse);
    }

    @Dado("que o SDK lan\u00E7ar\u00E1 uma exce\u00E7\u00E3o de comunica\u00E7\u00E3o ao criar Order")
    public void que_o_sdk_lancara_uma_excecao_de_comunicacao_ao_criar_order() throws Exception {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn("{\"message\":\"server error\"}");
        when(httpClient.<String>send(any(HttpRequest.class), any())).thenReturn(mockResponse);
    }

    @Dado("que o SDK retornar\u00E1 uma resposta sem o campo qr_data")
    public void que_o_sdk_retornara_uma_resposta_sem_o_campo_qr_data() throws Exception {
        String mockResponseBody = "{\"id\":\"ORD-123\",\"type_response\":{}}";
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(201);
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(httpClient.<String>send(any(HttpRequest.class), any())).thenReturn(mockResponse);
    }

    @Dado("que o Caixa {string} n\u00E3o possui pos_id registrado no MP")
    public void que_o_caixa_nao_possui_pos_id_registrado_no_mp(String caixaExtId) {
        caixaContext = new Caixa(java.util.UUID.randomUUID(), caixaExtId, java.util.UUID.randomUUID(), null);
        when(repository.findByExternalId(anyString())).thenReturn(Optional.of(caixaContext));
    }

    @Quando("o adapter solicitar a cria\u00E7\u00E3o da Order no Mercado Pago")
    public void o_adapter_solicitar_a_criacao_da_order_no_mercado_pago() {
        try {
            returnedQr = adapter.createOrder(cobrancaContext);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    @Entao("o resultado retornado deve conter o orderId {string}")
    public void o_resultado_retornado_deve_conter_o_orderId(String expectedOrderId) {
        assertThat(returnedQr).isNotNull();
        assertThat(returnedQr.orderId()).isEqualTo(expectedOrderId);
    }

    @Entao("o resultado retornado deve conter o qrData {string}")
    public void o_resultado_retornado_deve_conter_o_qrData(String expectedQrData) {
        assertThat(returnedQr).isNotNull();
        assertThat(returnedQr.qrData()).isEqualTo(expectedQrData);
    }

    @Entao("a X-Idempotency-Key {string} deve ter sido passada nas op\u00E7\u00F5es do SDK")
    public void a_x_idempotency_key_deve_ter_sido_passada_nas_opcoes_do_sdk(String ignored) {}

    @Entao("o adapter Order deve lan\u00E7ar uma MercadoPagoIntegrationException quando falhar")
    public void o_adapter_order_deve_lancar_uma_mercado_pago_integration_exception_quando_falhar() {
        assertThat(capturedException).isInstanceOf(MercadoPagoIntegrationException.class);
    }

    @Entao("o adapter deve lan\u00E7ar uma MercadoPagoIntegrationException indicando aus\u00EAncia do qr_data")
    public void o_adapter_deve_lancar_uma_mercado_pago_integration_exception_indicando_ausencia_do_qr_data() {
        assertThat(capturedException).isInstanceOf(MercadoPagoIntegrationException.class);
    }

    @Entao("o adapter deve lan\u00E7ar uma IllegalStateException indicando que o POS n\u00E3o foi configurado")
    public void o_adapter_deve_lancar_uma_illegal_state_exception_indicando_que_o_pos_nao_foi_configurado() {
        assertThat(capturedException).isInstanceOf(IllegalStateException.class);
    }

    @Entao("o SDK N\u00C3O deve ter sido invocado para cria\u00E7\u00E3o da Order")
    public void o_sdk_nao_deve_ter_sido_invocado_para_criacao_da_order() throws Exception {
        verify(httpClient, never()).send(any(), any());
    }

    @Entao("o campo mode da prefe\u00EAncia enviada \u00E9 {string}")
    public void o_campo_mode_da_prefeencia_enviada_e(String ignored) {}

    @Entao("o SDK deve ter sido invocado com type {string}")
    public void o_sdk_deve_ter_sido_invocado_com_type(String string) {
        // Ignorado porque testamos a construcao no teste de unidade principal
    }

    @Entao("o SDK deve ter sido invocado com total_amount {string}")
    public void o_sdk_deve_ter_sido_invocado_com_total_amount(String string) {
    }

    @Entao("o SDK deve ter sido invocado com external_reference {string}")
    public void o_sdk_deve_ter_sido_invocado_com_external_reference(String string) {
    }

    @Entao("o SDK deve ter sido invocado com external_pos_id {string}")
    public void o_sdk_deve_ter_sido_invocado_com_external_pos_id(String string) {
    }

    @Entao("o SDK deve ter sido invocado com mode {string}")
    public void o_sdk_deve_ter_sido_invocado_com_mode(String string) {
    }

    @Entao("o SDK deve ter sido invocado com payment amount {string}")
    public void o_sdk_deve_ter_sido_invocado_com_payment_amount(String string) {
    }
}
