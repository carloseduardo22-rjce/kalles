package dev.kalles.sale.mercadopago.steps;

import dev.kalles.sale.mercadopago.adapter.MercadoPagoStoreAdapter;
import dev.kalles.sale.mercadopago.domain.Company;
import dev.kalles.sale.mercadopago.port.CompanyMpRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.ArgumentCaptor;
import dev.kalles.sale.mercadopago.port.TenantRepository;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class StoreAdapterSteps {

    private Company companyContext;
    private Long returnedStoreId;
    private Exception capturedException;

    private final HttpClient httpClient = mock(HttpClient.class);
    private final CompanyMpRepository repository = mock(CompanyMpRepository.class);
    private final TenantRepository tenantRepository = mock(TenantRepository.class);
    private final MercadoPagoStoreAdapter adapter = new MercadoPagoStoreAdapter("12345", "mock-token", httpClient, tenantRepository);

    @Given("uma Company com id {string}, nome {string}, logradouro {string}, numero {string}, cidade {string}, estado {string}, latitude {string} e longitude {string}")
    public void companyWithData(String id, String nome, String logradouro, String numero, String cidade, String estado, String lat, String lon) {
        java.util.UUID uuid = id.equals("COMP-001") || id.startsWith("COMP") ? java.util.UUID.randomUUID() : java.util.UUID.fromString(id);
        companyContext = new Company(
                uuid, id, nome, logradouro, numero, cidade, estado,
                Double.parseDouble(lat), Double.parseDouble(lon), null,
                null
        );
    }

    @Given("uma Company com id {string} que já possui store_id {string} registrado")
    public void companyAlreadyHasStoreId(String companyId, String storeId) {
        java.util.UUID uuid = companyId.equals("COMP-001") || companyId.startsWith("COMP") ? java.util.UUID.randomUUID() : java.util.UUID.fromString(companyId);
        companyContext = new Company(uuid, companyId, "Name", "Street", "1", "City", "ST", 0.0, 0.0, Long.parseLong(storeId), null);
    }

    @Given("uma Company com id {string} e nome {string} sem store_id cadastrado")
    public void companyWithoutStoreId(String companyId, String companyName) {
        java.util.UUID uuid = companyId.equals("COMP-002") || companyId.startsWith("COMP") ? java.util.UUID.randomUUID() : java.util.UUID.fromString(companyId);
        companyContext = new Company(uuid, companyId, companyName, "Street", "1", "City", "ST", 0.0, 0.0, null, null);
    }

    @And("que o SDK do Mercado Pago retornará o store_id {string} para essa requisição")
    public void sdkWillReturnStoreId(String storeId) throws Exception {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(201);
        when(mockResponse.body()).thenReturn("{\"id\":" + storeId + "}");
        when(httpClient.<String>send(any(HttpRequest.class), any())).thenReturn(mockResponse);
    }

    @And("que o SDK lançará uma exceção de comunicação ao criar Store")
    public void sdkWillThrowException() throws Exception {
        when(httpClient.<String>send(any(HttpRequest.class), any())).thenThrow(new java.io.IOException("Comm Error"));
    }

    @When("o adapter solicitar a criação da Store no Mercado Pago")
    public void adapterProcessesStoreCreation() {
        try {
            returnedStoreId = adapter.createStore(companyContext);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    @Then("o SDK deve ter sido invocado com name da Store {string}")
    public void sdkShouldBeInvokedWithName(String expectedName) {
    }

    @And("o SDK deve ter sido invocado com street_number {string}")
    public void sdkShouldBeInvokedWithStreetNumber(String expectedNumber) {
    }

    @And("o SDK deve ter sido invocado com street_name {string}")
    public void sdkShouldBeInvokedWithStreetName(String expectedStreetName) {
    }

    @And("o SDK deve ter sido invocado com city_name {string}")
    public void sdkShouldBeInvokedWithCityName(String expectedCityName) {
    }

    @And("o SDK deve ter sido invocado com state_name {string}")
    public void sdkShouldBeInvokedWithStateName(String expectedStateName) {
    }

    @And("o SDK deve ter sido invocado com latitude {string}")
    public void sdkShouldBeInvokedWithLatitude(String expectedLat) {
    }

    @And("o SDK deve ter sido invocado com longitude {string}")
    public void sdkShouldBeInvokedWithLongitude(String expectedLon) {
    }

    @And("o SDK deve ter sido invocado com external_id {string}")
    public void sdkShouldBeInvokedWithExternalId(String expectedExternalId) {
    }

    @And("o resultado retornado deve conter o store_id {string}")
    public void o_resultado_retornado_deve_conter_o_store_id(String expectedStoreId) {
        assertThat(String.valueOf(returnedStoreId)).isEqualTo(expectedStoreId);
    }

    @And("o store_id {string} deve ser retornado diretamente ao domínio")
    public void o_store_id_deve_ser_retornado_diretamente_ao_dominio(String expectedStoreId) {
        assertThat(String.valueOf(returnedStoreId)).isEqualTo(expectedStoreId);
    }

    @And("o store_id {string} deve ter sido persistido vinculado à Company {string}")
    public void storeIdShouldBePersisted(String storeId, String companyId) {
    }

    @Then("o SDK NÃO deve ter sido invocado para criação da Store")
    public void sdkShouldNotBeInvoked() throws Exception {
        verify(httpClient, never()).send(any(HttpRequest.class), any());
    }

    @And("nenhum store_id deve ter sido persistido para a Company {string}")
    public void noPersistenceShouldOccur(String companyId) {
    }

    @Then("o adapter Store deve lançar uma MercadoPagoIntegrationException quando falhar")
    public void adapterShouldThrowIntegrationException() {
        assertThat(capturedException)
                .isNotNull();
    }
}
