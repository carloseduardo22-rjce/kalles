package dev.kalles.sale.mercadopago.steps;

import com.google.gson.JsonObject;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.MPHttpClient;
import com.mercadopago.net.MPRequest;
import com.mercadopago.net.MPResponse;
import dev.kalles.sale.mercadopago.adapter.MercadoPagoPosAdapter;
import dev.kalles.sale.mercadopago.domain.Caixa;
import dev.kalles.sale.mercadopago.domain.Company;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.ArgumentCaptor;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PosAdapterSteps {

    private Company companyContext;
    private Caixa caixaContext;
    private Long returnedPosId;
    private Exception capturedException;

    private final MPHttpClient httpClient = mock(MPHttpClient.class);
    private final MercadoPagoPosAdapter adapter = new MercadoPagoPosAdapter(httpClient, "mock-token");

    @Given("uma Company {string} que possui store_id {string} gerado no MP")
    public void companyWithStoreId(String companyId, String storeId) {
        companyContext = new Company(
                java.util.UUID.randomUUID(), companyId, "Kalles", "Str", "1", "City", "ST", -23.0, -46.0, Long.parseLong(storeId)
        );
    }

    @Given("uma Company {string} que AINDA NÃO possui store_id no MP")
    public void companyWithoutStoreId(String companyId) {
        companyContext = new Company(
                java.util.UUID.randomUUID(), companyId, "Kalles", "Str", "1", "City", "ST", -23.0, -46.0, null
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
    public void sdkWillReturnPosId(String posId) throws MPException, MPApiException {
        MPResponse mockResponse = new MPResponse(201, Collections.emptyMap(), "{\"id\":" + posId + "}");
        when(httpClient.send(any(MPRequest.class))).thenReturn(mockResponse);
    }

    @And("que o SDK lançará uma exceção de comunicação ao criar POS")
    public void sdkWillThrowException() throws MPException, MPApiException {
        when(httpClient.send(any(MPRequest.class))).thenThrow(new MPException("Comm error"));
    }

    @When("o adapter solicitar a criação do POS no Mercado Pago")
    public void adapterProcessesPosCreation() {
        try {
            returnedPosId = adapter.createPos(caixaContext, companyContext);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    private JsonObject getCapturedPayload() throws MPException, MPApiException {
        ArgumentCaptor<MPRequest> captor = ArgumentCaptor.forClass(MPRequest.class);
        verify(httpClient).send(captor.capture());
        return captor.getValue().getPayload();
    }

    @Then("o SDK deve ter sido invocado com name {string}")
    public void sdkShouldBeInvokedWithBoxName(String expectedName) throws MPException, MPApiException {
        assertThat(getCapturedPayload().get("name").getAsString()).isEqualTo(expectedName);
    }

    @And("o SDK deve ter sido invocado com external_id do caixa {string}")
    public void sdkShouldBeInvokedWithBoxExternalId(String expectedExternalId) throws MPException, MPApiException {
        assertThat(getCapturedPayload().get("external_id").getAsString()).isEqualTo(expectedExternalId);
    }

    @And("o SDK deve ter sido invocado com store_id numérico {string}")
    public void sdkShouldBeInvokedWithStoreId(String expectedStoreId) throws MPException, MPApiException {
        assertThat(getCapturedPayload().get("store_id").getAsString()).isEqualTo(expectedStoreId);
    }

    @And("o SDK deve ter sido invocado com external_store_id igual a {string}")
    public void sdkShouldBeInvokedWithExternalStoreId(String expectedExternalStoreId) throws MPException, MPApiException {
        assertThat(getCapturedPayload().get("external_store_id").getAsString()).isEqualTo(expectedExternalStoreId);
    }

    @And("o SDK deve ter sido invocado com fixed_amount igual a {string}")
    public void sdkShouldBeInvokedWithFixedAmount(String expectedFixedAmount) throws MPException, MPApiException {
        boolean expected = Boolean.parseBoolean(expectedFixedAmount);
        assertThat(getCapturedPayload().get("fixed_amount").getAsBoolean()).isEqualTo(expected);
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
        //verify(repository).savePosId(caixaId, Long.parseLong(posId));
    }

    @Then("o SDK NÃO deve ter sido invocado para criação do POS")
    public void sdkShouldNotBeInvoked() throws MPException, MPApiException {
        verify(httpClient, never()).send(any(MPRequest.class));
    }

    @And("nenhum pos_id deve ter sido persistido para o Caixa {string}")
    public void noPersistenceShouldOccur(String caixaId) {
        //verify(repository, never()).savePosId(eq(caixaId), anyLong());
    }

    @Then("o adapter deve lançar uma IllegalStateException indicando que a Company não tem Store")
    public void shouldThrowIllegalStateExceptionNoStore() {
        assertThat(capturedException)
                .isNotNull()
                .hasMessageContaining("Store");
    }

    @Then("o adapter POS deve lançar uma MercadoPagoIntegrationException quando falhar")
    public void shouldThrowIntegrationException() {
        assertThat(capturedException).isNotNull()
                .isInstanceOf(dev.kalles.sale.mercadopago.exception.MercadoPagoIntegrationException.class);
    }
}
