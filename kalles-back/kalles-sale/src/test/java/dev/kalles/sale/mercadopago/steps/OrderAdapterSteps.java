package dev.kalles.sale.mercadopago.steps;

import com.google.gson.JsonObject;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.MPHttpClient;
import com.mercadopago.net.MPRequest;
import com.mercadopago.net.MPResponse;
import dev.kalles.sale.mercadopago.adapter.MercadoPagoOrderAdapter;
import dev.kalles.sale.mercadopago.domain.Caixa;
import dev.kalles.sale.mercadopago.domain.CobrancaQr;
import dev.kalles.sale.mercadopago.domain.ResultadoQr;
import dev.kalles.sale.mercadopago.port.CaixaMpRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OrderAdapterSteps {

    private CobrancaQr cobrancaContext;
    private Caixa caixaContext;
    private ResultadoQr returnedResult;
    private Exception capturedException;

    private final MPHttpClient httpClient = mock(MPHttpClient.class);
    private final CaixaMpRepository caixaMpRepository = mock(CaixaMpRepository.class);
    private final MercadoPagoOrderAdapter adapter = new MercadoPagoOrderAdapter(httpClient, caixaMpRepository);

    @Given("um Caixa com external_id {string} que já possui pos_id {string} registrado no MP")
    public void caixaWithPosId(String externalId, String posId) {
        caixaContext = new Caixa(externalId, "Caixa", "COMP-001", Long.parseLong(posId));
        when(caixaMpRepository.findById(externalId)).thenReturn(Optional.of(caixaContext));
    }

    @Given("que o Caixa {string} não possui pos_id registrado no MP")
    public void caixaWithoutPosId(String externalId) {
        caixaContext = new Caixa(externalId, "No POS", "COMP-001", null);
        when(caixaMpRepository.findById(externalId)).thenReturn(Optional.of(caixaContext));
    }

    @Given("uma intenção de cobrança com pedidoId {string}, valor {string}, caixa {string} e idempotencyKey {string}")
    public void cobrancaIntent(String pedidoId, String valor, String caixaExtId, String idempotencyKey) {
        cobrancaContext = new CobrancaQr(pedidoId, new BigDecimal(valor), caixaExtId, idempotencyKey);
    }

    @And("que o SDK retornará order_id {string} e qr_data {string}")
    public void sdkReturnsOrderWithQrData(String orderId, String qrData) throws MPException, MPApiException {
        String json = "{\"id\":\"" + orderId + "\",\"type_response\":{\"qr_data\":\"" + qrData + "\"}}";
        MPResponse mockResponse = new MPResponse(201, Collections.emptyMap(), json);
        when(httpClient.send(any(MPRequest.class))).thenReturn(mockResponse);
    }

    @And("que o SDK lançará uma exceção de comunicação ao criar Order")
    public void sdkWillThrowException() throws MPException, MPApiException {
        when(httpClient.send(any(MPRequest.class))).thenThrow(new MPException("Comm Error"));
    }

    @And("que o SDK retornará uma resposta sem o campo qr_data")
    public void sdkReturnsOrderWithoutQrData() throws MPException, MPApiException {
        String json = "{\"id\":\"ORD-NO-QR\",\"type_response\":{}}";
        MPResponse mockResponse = new MPResponse(201, Collections.emptyMap(), json);
        when(httpClient.send(any(MPRequest.class))).thenReturn(mockResponse);
    }

    @When("o adapter solicitar a criação da Order no Mercado Pago")
    public void adapterRequestsOrderCreation() {
        try {
            returnedResult = adapter.createOrder(cobrancaContext);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    private JsonObject getCapturedPayload() throws MPException, MPApiException {
        ArgumentCaptor<MPRequest> captor = ArgumentCaptor.forClass(MPRequest.class);
        verify(httpClient).send(captor.capture());
        return captor.getValue().getPayload();
    }

    @Then("o SDK deve ter sido invocado com type {string}")
    public void sdkInvokedWithType(String expectedType) throws MPException, MPApiException {
        assertThat(getCapturedPayload().get("type").getAsString()).isEqualTo(expectedType);
    }

    @And("o SDK deve ter sido invocado com total_amount {string}")
    public void sdkInvokedWithTotalAmount(String expectedAmount) throws MPException, MPApiException {
        assertThat(getCapturedPayload().get("total_amount").getAsDouble()).isEqualByComparingTo(Double.parseDouble(expectedAmount));
    }

    @And("o SDK deve ter sido invocado com external_reference {string}")
    public void sdkInvokedWithExternalReference(String expectedExternalReference) throws MPException, MPApiException {
        assertThat(getCapturedPayload().get("external_reference").getAsString()).isEqualTo(expectedExternalReference);
    }

    @And("o SDK deve ter sido invocado com external_pos_id {string}")
    public void sdkInvokedWithExternalPosId(String expectedExternalPosId) throws MPException, MPApiException {
        assertThat(getCapturedPayload().getAsJsonObject("config").getAsJsonObject("qr").get("external_pos_id").getAsString()).isEqualTo(expectedExternalPosId);
    }

    @And("o SDK deve ter sido invocado com mode {string}")
    public void sdkInvokedWithMode(String expectedMode) throws MPException, MPApiException {
        assertThat(getCapturedPayload().getAsJsonObject("config").getAsJsonObject("qr").get("mode").getAsString()).isEqualTo(expectedMode);
    }

    @And("o SDK deve ter sido invocado com payment amount {string}")
    public void sdkInvokedWithPaymentAmount(String expectedAmount) throws MPException, MPApiException {
        double paymentAmount = getCapturedPayload().getAsJsonObject("transactions").getAsJsonArray("payments").get(0).getAsJsonObject().get("amount").getAsDouble();
        assertThat(paymentAmount).isEqualByComparingTo(Double.parseDouble(expectedAmount));
    }

    @And("a X-Idempotency-Key {string} deve ter sido passada nas opções do SDK")
    public void idempotencyKeyPassedInOptions(String expectedKey) throws MPException, MPApiException {
        ArgumentCaptor<MPRequest> captor = ArgumentCaptor.forClass(MPRequest.class);
        verify(httpClient).send(captor.capture());
        assertThat(captor.getValue().getHeaders().get("X-Idempotency-Key")).isEqualTo(expectedKey);
    }

    @And("o resultado retornado deve conter o orderId {string}")
    public void resultContainsOrderId(String expectedOrderId) {
        assertThat(returnedResult.orderId()).isEqualTo(expectedOrderId);
    }

    @And("o resultado retornado deve conter o qrData {string}")
    public void resultContainsQrData(String expectedQrData) {
        assertThat(returnedResult.qrData()).isEqualTo(expectedQrData);
    }

    @Then("o adapter deve lançar uma IllegalStateException indicando que o POS não foi configurado")
    public void shouldThrowIllegalStateExceptionNoPos() {
        assertThat(capturedException)
                .isNotNull()
                .hasMessageContaining("POS");
    }

    @Then("o adapter deve lançar uma MercadoPagoIntegrationException indicando ausência do qr_data")
    public void shouldThrowExceptionNoQrData() {
        assertThat(capturedException)
                .isNotNull()
                .hasMessageContaining("qr_data");
    }

    @And("o SDK NÃO deve ter sido invocado para criação da Order")
    public void sdkShouldNotBeInvokedForOrder() throws MPException, MPApiException {
        verify(httpClient, never()).send(any(MPRequest.class));
    }

    @Then("o adapter Order deve lançar uma MercadoPagoIntegrationException quando falhar")
    public void adapterShouldThrowIntegrationException() {
        assertThat(capturedException)
                .isNotNull()
                .isInstanceOf(dev.kalles.sale.mercadopago.exception.MercadoPagoIntegrationException.class);
    }
}
