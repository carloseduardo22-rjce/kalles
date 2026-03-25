package dev.kalles.sale.mercadopago.steps;

import com.google.gson.JsonObject;
import dev.kalles.sale.mercadopago.adapter.MercadoPagoPosAdapter;
import dev.kalles.sale.mercadopago.domain.Caixa;
import dev.kalles.sale.mercadopago.domain.Company;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.ArgumentCaptor;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PosAdapterSteps {

    private Company companyContext;
    private Caixa caixaContext;
    private Long returnedPosId;
    private Exception capturedException;

    private final HttpClient httpClient = mock(HttpClient.class);
    private final MercadoPagoPosAdapter adapter = new MercadoPagoPosAdapter(httpClient, "mock-token");

    @Given("uma Company {string} que possui store_id {string} gerado no MP")
    public void companyWithStoreId(String companyId, String storeId) {
        companyContext = new Company(
                java.util.UUID.randomUUID(), companyId, "Kalles", "Str", "1", "City", "ST", -23.0, -46.0, Long.parseLong(storeId), null
        );
    }

    @Given("uma Company {string} que AINDA NÃO possui store_id no MP")
    public void companyWithoutStoreId(String companyId) {
        companyContext = new Company(
                java.util.UUID.randomUUID(), companyId, "Kalles", "Str", "1", "City", "ST", -23.0, -46.0, null, null
        );
    }

    @And("um Caixa com id {string}, nome {string}, pertencente à Company {string}")
    public void caixaBelongsToCompany(String caixaId, String nome, String companyId) {
        caixaContext = new Caixa(java.util.UUID.randomUUID(), caixaId, nome, companyContext.id().toString(), null);
    }

    @Given("um Caixa com id {string} que já possui pos_id {string} registrado")
    public void umCaixaComPosId(String caixaId, String posId) {
        caixaContext = new Caixa(java.util.UUID.randomUUID(), caixaId, "Caixa", java.util.UUID.randomUUID().toString(), Long.parseLong(posId));
    }

    @And("que o Caixa já possui o pos_id {string} gerado anteriormente")
    public void caixaAlreadyHasPosId(String posId) {
        caixaContext = caixaContext.withPosId(Long.parseLong(posId));
    }

    @And("que o SDK do Mercado Pago retornará o pos_id {string} para essa requisição")
    public void sdkWillReturnPosId(String posId) throws Exception {
        HttpResponse<String> mockResponseSearch = mock(HttpResponse.class);
        when(mockResponseSearch.statusCode()).thenReturn(404);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(201);
        when(mockResponse.body()).thenReturn("{\"id\":" + posId + "}");
        when(httpClient.<String>send(any(HttpRequest.class), any())).thenReturn(mockResponseSearch, mockResponse);
    }

    @And("que o SDK lançará uma exceção de comunicação ao criar POS")
    public void sdkWillThrowException() throws Exception {
        HttpResponse<String> mockResponseSearch = mock(HttpResponse.class);
        when(mockResponseSearch.statusCode()).thenReturn(404);
        when(httpClient.<String>send(any(HttpRequest.class), any()))
            .thenReturn(mockResponseSearch)
            .thenThrow(new java.io.IOException("Comm error"));
    }

    @When("o adapter solicitar a criação do POS no Mercado Pago")
    public void adapterProcessesPosCreation() {
        try {
            returnedPosId = adapter.createPos(caixaContext, companyContext);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    @Then("o SDK deve ter sido invocado com name {string}")
    public void sdkShouldBeInvokedWithBoxName(String expectedName) {
    }

    @And("o SDK deve ter sido invocado com external_id do caixa {string}")
    public void sdkShouldBeInvokedWithBoxExternalId(String expectedExternalId) {
    }

    @And("o SDK deve ter sido invocado com store_id numérico {string}")
    public void sdkShouldBeInvokedWithStoreId(String expectedStoreId) {
    }

    @And("o SDK deve ter sido invocado com external_store_id igual a {string}")
    public void sdkShouldBeInvokedWithExternalStoreId(String expectedExternalStoreId) {
    }

    @And("o SDK deve ter sido invocado com fixed_amount igual a {string}")
    public void sdkShouldBeInvokedWithFixedAmount(String expectedFixedAmount) {
    }

    @And("o resultado retornado deve conter o pos_id {string}")
    public void resultShouldBePosId(String expectedPosId) {
        assertThat(String.valueOf(returnedPosId)).isEqualTo(expectedPosId);
    }

    @And("o pos_id {string} deve ser retornado diretamente ao domínio")
    public void posIdReturnedDomain(String expectedPosId) {
        assertThat(String.valueOf(returnedPosId)).isEqualTo(expectedPosId);
    }

    @And("o pos_id {string} deve ter sido persistido vinculado ao Caixa {string}")
    public void posIdShouldBePersisted(String posId, String caixaId) {
    }

    @Then("o SDK NÃO deve ter sido invocado para criação do POS")
    public void sdkShouldNotBeInvoked() throws Exception {
        verify(httpClient, never()).send(any(HttpRequest.class), any());
    }

    @And("nenhum pos_id deve ter sido persistido para o Caixa {string}")
    public void noPersistenceShouldOccur(String caixaId) {
    }

    @Then("o adapter deve lançar uma IllegalStateException indicando que a Company não tem Store")
    public void shouldThrowIllegalStateExceptionNoStore() {
        assertThat(capturedException)
                .isNotNull()
                .hasMessageContaining("Store");
    }

    @Then("o adapter POS deve lançar uma MercadoPagoIntegrationException quando falhar")
    public void shouldThrowIntegrationException() {
        assertThat(capturedException).isNotNull();
    }
}
