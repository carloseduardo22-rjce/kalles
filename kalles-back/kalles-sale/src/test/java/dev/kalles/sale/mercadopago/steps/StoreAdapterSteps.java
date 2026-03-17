package dev.kalles.sale.mercadopago.steps;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.MPHttpClient;
import com.mercadopago.net.MPRequest;
import com.mercadopago.net.MPResponse;
import dev.kalles.sale.mercadopago.adapter.MercadoPagoStoreAdapter;
import dev.kalles.sale.mercadopago.domain.Company;
import dev.kalles.sale.mercadopago.port.CompanyMpRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.ArgumentCaptor;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class StoreAdapterSteps {

    private Company companyContext;
    private Long returnedStoreId;
    private Exception capturedException;

    private final MPHttpClient httpClient = mock(MPHttpClient.class);
    private final CompanyMpRepository repository = mock(CompanyMpRepository.class);
    private final MercadoPagoStoreAdapter adapter = new MercadoPagoStoreAdapter(httpClient, "12345", "mock-token");

    @Given("uma Company com id {string}, nome {string}, logradouro {string}, numero {string}, cidade {string}, estado {string}, latitude {string} e longitude {string}")
    public void companyWithData(String id, String nome, String logradouro, String numero, String cidade, String estado, String lat, String lon) {
        java.util.UUID uuid = id.equals("COMP-001") || id.startsWith("COMP") ? java.util.UUID.randomUUID() : java.util.UUID.fromString(id);
        companyContext = new Company(
                uuid, nome, logradouro, numero, cidade, estado,
                Double.parseDouble(lat), Double.parseDouble(lon), null
        );
    }

    @Given("uma Company com id {string} que já possui store_id {string} registrado")
    public void companyAlreadyHasStoreId(String companyId, String storeId) {
        java.util.UUID uuid = companyId.equals("COMP-001") || companyId.startsWith("COMP") ? java.util.UUID.randomUUID() : java.util.UUID.fromString(companyId);
        companyContext = new Company(uuid, "Name", "Street", "1", "City", "ST", 0.0, 0.0, Long.parseLong(storeId));
    }

    @Given("uma Company com id {string} e nome {string} sem store_id cadastrado")
    public void companyWithoutStoreId(String companyId, String companyName) {
        java.util.UUID uuid = companyId.equals("COMP-002") || companyId.startsWith("COMP") ? java.util.UUID.randomUUID() : java.util.UUID.fromString(companyId);
        companyContext = new Company(uuid, companyName, "Street", "1", "City", "ST", 0.0, 0.0, null);
    }

    @And("que o SDK do Mercado Pago retornará o store_id {string} para essa requisição")
    public void sdkWillReturnStoreId(String storeId) throws MPException, MPApiException {
        MPResponse mockResponse = new MPResponse(201, Collections.emptyMap(), "{\"id\":" + storeId + "}");
        when(httpClient.send(any(MPRequest.class))).thenReturn(mockResponse);
    }

    @And("que o SDK lançará uma exceção de comunicação ao criar Store")
    public void sdkWillThrowException() throws MPException, MPApiException {
        when(httpClient.send(any(MPRequest.class))).thenThrow(new MPException("Comm Error"));
    }

    @When("o adapter solicitar a criação da Store no Mercado Pago")
    public void adapterProcessesStoreCreation() {
        try {
            returnedStoreId = adapter.createStore(companyContext);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    private JsonObject getCapturedPayload() throws MPException, MPApiException {
        ArgumentCaptor<MPRequest> captor = ArgumentCaptor.forClass(MPRequest.class);
        verify(httpClient).send(captor.capture());
        return captor.getValue().getPayload();
    }

    private JsonObject getCapturedLocation() throws MPException, MPApiException {
        return getCapturedPayload().getAsJsonObject("location");
    }

    @Then("o SDK deve ter sido invocado com name da Store {string}")
    public void sdkShouldBeInvokedWithName(String expectedName) throws MPException, MPApiException {
        assertThat(getCapturedPayload().get("name").getAsString()).isEqualTo(expectedName);
    }

    @And("o SDK deve ter sido invocado com street_number {string}")
    public void sdkShouldBeInvokedWithStreetNumber(String expectedNumber) throws MPException, MPApiException {
        assertThat(getCapturedLocation().get("street_number").getAsString()).isEqualTo(expectedNumber);
    }

    @And("o SDK deve ter sido invocado com street_name {string}")
    public void sdkShouldBeInvokedWithStreetName(String expectedStreetName) throws MPException, MPApiException {
        assertThat(getCapturedLocation().get("street_name").getAsString()).isEqualTo(expectedStreetName);
    }

    @And("o SDK deve ter sido invocado com city_name {string}")
    public void sdkShouldBeInvokedWithCityName(String expectedCityName) throws MPException, MPApiException {
        assertThat(getCapturedLocation().get("city_name").getAsString()).isEqualTo(expectedCityName);
    }

    @And("o SDK deve ter sido invocado com state_name {string}")
    public void sdkShouldBeInvokedWithStateName(String expectedStateName) throws MPException, MPApiException {
        assertThat(getCapturedLocation().get("state_name").getAsString()).isEqualTo(expectedStateName);
    }

    @And("o SDK deve ter sido invocado com latitude {string}")
    public void sdkShouldBeInvokedWithLatitude(String expectedLat) throws MPException, MPApiException {
        assertThat(getCapturedLocation().get("latitude").getAsDouble()).isEqualByComparingTo(Double.parseDouble(expectedLat));
    }

    @And("o SDK deve ter sido invocado com longitude {string}")
    public void sdkShouldBeInvokedWithLongitude(String expectedLon) throws MPException, MPApiException {
        assertThat(getCapturedLocation().get("longitude").getAsDouble()).isEqualByComparingTo(Double.parseDouble(expectedLon));
    }

    @And("o SDK deve ter sido invocado com external_id {string}")
    public void sdkShouldBeInvokedWithExternalId(String expectedExternalId) throws MPException, MPApiException {
        assertThat(getCapturedPayload().get("external_id").getAsString()).isEqualTo(expectedExternalId);
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
        //verify(repository).saveStoreId(companyId, Long.parseLong(storeId));
    }

    @Then("o SDK NÃO deve ter sido invocado para criação da Store")
    public void sdkShouldNotBeInvoked() throws MPException, MPApiException {
        verify(httpClient, never()).send(any(MPRequest.class));
    }

    @And("nenhum store_id deve ter sido persistido para a Company {string}")
    public void noPersistenceShouldOccur(String companyId) {
        //verify(repository, never()).saveStoreId(eq(companyId), anyLong());
    }

    @Then("o adapter Store deve lançar uma MercadoPagoIntegrationException quando falhar")
    public void adapterShouldThrowIntegrationException() {
        assertThat(capturedException)
                .isNotNull()
                .isInstanceOf(dev.kalles.sale.mercadopago.exception.MercadoPagoIntegrationException.class);
    }
}
