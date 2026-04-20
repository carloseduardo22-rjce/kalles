package dev.kalles.sale.client.integration;

import dev.kalles.sale.client.support.AbstractClientApiSupport;
import dev.kalles.sale.payment.support.LocalHttpTestClient;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@Tag("integration")
class ClientApiIntegrationTest extends AbstractClientApiSupport {

    @BeforeEach
    void setUp() {
        resetClientScenario();
    }

    @Test
    void shouldCreateClientInsideActiveCompany() {
        AuthContext auth = authenticateTenantAdminWithCsrf();

        givenAuthenticated(auth, companyAId)
                .body(Map.of(
                        "name", "Maria Souza",
                        "birthDate", "1990-05-20",
                        "gender", "F",
                        "cpf", "52998224725",
                        "codeCountry", "+55",
                        "cellphone", "11999999999"
                ))
                .when()
                .post("/api/clients")
                .then()
                .statusCode(201)
                .body("name", equalTo("Maria Souza"))
                .body("cpf", equalTo("52998224725"));
    }

    @Test
    void shouldRejectDuplicateCpfWithinSameCompany() {
        AuthContext auth = authenticateTenantAdminWithCsrf();
        seedClient(companyAId, "Maria Existente", "52998224725");

        givenAuthenticated(auth, companyAId)
                .body(Map.of(
                        "name", "Maria Nova",
                        "birthDate", "1990-05-20",
                        "gender", "F",
                        "cpf", "52998224725",
                        "codeCountry", "+55",
                        "cellphone", "11999999999"
                ))
                .when()
                .post("/api/clients")
                .then()
                .statusCode(400)
                .body("detail", equalTo("Já existe um cliente com o CPF informado nesta filial."));
    }

    @Test
    void shouldAllowSameCpfInDifferentCompanies() {
        AuthContext auth = authenticateTenantAdminWithCsrf();
        seedClient(companyBId, "Maria Loja B", "52998224725");

        givenAuthenticated(auth, companyAId)
                .body(Map.of(
                        "name", "Maria Loja A",
                        "birthDate", "1990-05-20",
                        "gender", "F",
                        "cpf", "52998224725",
                        "codeCountry", "+55",
                        "cellphone", "11999999999"
                ))
                .when()
                .post("/api/clients")
                .then()
                .statusCode(201)
                .body("cpf", equalTo("52998224725"));
    }

    @Test
    void shouldReturnOnlyClientsFromActiveCompany() {
        AuthContext auth = authenticateTenantAdminWithCsrf();
        seedClient(companyAId, "Ana", "52998224725");
        seedClient(companyBId, "Bruna", "28625587887");

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/clients",
                Map.of(
                        "Cookie", "kalles_auth_token=" + auth.authCookie(),
                        "X-Company-ID", companyAId.toString()
                )
        );

        response.then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].name", equalTo("Ana"));
    }

    @Test
    void shouldReturnPagedClientsOnlyFromActiveCompany() {
        AuthContext auth = authenticateTenantAdminWithCsrf();
        seedClient(companyAId, "Ana", "52998224725");
        seedClient(companyAId, "Bianca", "28625587887");
        seedClient(companyBId, "Carla", "39053344705");

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/clients/page?page=0&size=1",
                Map.of(
                        "Cookie", "kalles_auth_token=" + auth.authCookie(),
                        "X-Company-ID", companyAId.toString()
                )
        );

        response.then()
                .statusCode(200)
                .body("content", hasSize(1))
                .body("totalElements", equalTo(2))
                .body("page", equalTo(0))
                .body("size", equalTo(1));
    }

    @Test
    void shouldReturnNotFoundWhenFetchingClientFromAnotherCompany() {
        AuthContext auth = authenticateTenantAdminWithCsrf();
        UUID foreignClientId = seedClient(companyBId, "Bruna", "28625587887").getId();

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/clients/" + foreignClientId,
                Map.of(
                        "Cookie", "kalles_auth_token=" + auth.authCookie(),
                        "X-Company-ID", companyAId.toString()
                )
        );

        response.then()
                .statusCode(404)
                .body("detail", equalTo("Cliente não encontrado: " + foreignClientId));
    }

    @Test
    void shouldRequireActiveCompanyToListClients() {
        String authCookie = loginAndExtractAuthCookie(TENANT_ADMIN_EMAIL);

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/clients",
                Map.of("Cookie", "kalles_auth_token=" + authCookie)
        );

        response.then()
                .statusCode(400)
                .body("code", equalTo("COMPANY_CONTEXT_REQUIRED"));
    }

    @Test
    void shouldDeleteClientOnlyInsideActiveCompany() {
        AuthContext auth = authenticateTenantAdminWithCsrf();
        UUID clientId = seedClient(companyAId, "Ana", "52998224725").getId();

        Response response = LocalHttpTestClient.delete(
                "http://localhost:" + port + "/api/clients/" + clientId,
                Map.of(
                        "Cookie", "kalles_auth_token=" + auth.authCookie() + "; XSRF-TOKEN=" + auth.csrfCookie(),
                        "X-Company-ID", companyAId.toString(),
                        "X-XSRF-TOKEN", auth.csrfToken(),
                        "Content-Type", "application/json"
                )
        );

        response.then().statusCode(204);
        assertThat(clientRepository.findById(clientId)).isEmpty();
    }
}
