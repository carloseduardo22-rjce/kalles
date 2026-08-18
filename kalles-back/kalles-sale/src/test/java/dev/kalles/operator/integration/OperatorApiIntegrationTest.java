package dev.kalles.operator.integration;

import dev.kalles.cashregister.enums.PermissionLevel;
import dev.kalles.operator.support.AbstractOperatorApiSupport;
import dev.kalles.testsupport.LocalHttpTestClient;
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
class OperatorApiIntegrationTest extends AbstractOperatorApiSupport {

    @BeforeEach
    void setUp() {
        resetOperatorScenario();
    }

    @Test
    void shouldCreateOperatorInsideActiveCompany() {
        AuthContext auth = authenticateTenantAdminWithCsrf();

        givenAuthenticated(auth, companyAId)
                .body(Map.of(
                        "name", "Maria Silva",
                        "code", "maria.silva",
                        "permissionLevel", "MANAGER"
                ))
                .when()
                .post("/api/operators")
                .then()
                .statusCode(201)
                .body("name", equalTo("Maria Silva"))
                .body("code", equalTo("maria.silva"))
                .body("permissionLevel", equalTo("MANAGER"));
    }

    @Test
    void shouldRejectDuplicateOperatorCodeWithinSameCompany() {
        AuthContext auth = authenticateTenantAdminWithCsrf();
        seedOperator(companyAId, "Maria Existente", "maria.silva", PermissionLevel.BASIC, true);

        givenAuthenticated(auth, companyAId)
                .body(Map.of(
                        "name", "Maria Nova",
                        "code", "maria.silva",
                        "permissionLevel", "MANAGER"
                ))
                .when()
                .post("/api/operators")
                .then()
                .statusCode(400)
                .body("detail", equalTo("Já existe um operador com o código informado nesta filial."));
    }

    @Test
    void shouldAllowSameOperatorCodeInDifferentCompanies() {
        AuthContext auth = authenticateTenantAdminWithCsrf();
        seedOperator(companyBId, "Maria Loja B", "maria.silva", PermissionLevel.BASIC, true);

        givenAuthenticated(auth, companyAId)
                .body(Map.of(
                        "name", "Maria Loja A",
                        "code", "maria.silva",
                        "permissionLevel", "MANAGER"
                ))
                .when()
                .post("/api/operators")
                .then()
                .statusCode(201)
                .body("code", equalTo("maria.silva"));
    }

    @Test
    void shouldReturnOnlyActiveOperatorsFromActiveCompany() {
        AuthContext auth = authenticateTenantAdminWithCsrf();
        seedOperator(companyAId, "Ana", "ana", PermissionLevel.BASIC, true);
        seedOperator(companyAId, "Bruno", "bruno", PermissionLevel.MANAGER, false);
        seedOperator(companyBId, "Carlos", "carlos", PermissionLevel.MANAGER, true);

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/operators",
                Map.of(
                        "Cookie", "kalles_auth_token=" + auth.authCookie(),
                        "X-Company-ID", companyAId.toString()
                )
        );

        response.then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].code", equalTo("ana"));
    }

    @Test
    void shouldReturnNotFoundWhenFetchingOperatorFromAnotherCompany() {
        AuthContext auth = authenticateTenantAdminWithCsrf();
        UUID foreignOperatorId = seedOperator(companyBId, "Carlos", "carlos", PermissionLevel.MANAGER, true).getId();

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/operators/" + foreignOperatorId,
                Map.of(
                        "Cookie", "kalles_auth_token=" + auth.authCookie(),
                        "X-Company-ID", companyAId.toString()
                )
        );

        response.then()
                .statusCode(404)
                .body("detail", equalTo("Operador não encontrado: " + foreignOperatorId));
    }

    @Test
    void shouldRequireActiveCompanyToListOperators() {
        String authCookie = loginAndExtractAuthCookie(TENANT_ADMIN_EMAIL);

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/operators",
                Map.of("Cookie", "kalles_auth_token=" + authCookie)
        );

        response.then()
                .statusCode(400)
                .body("code", equalTo("COMPANY_CONTEXT_REQUIRED"));
    }

    @Test
    void shouldDeactivateOperatorBySoftDelete() {
        AuthContext auth = authenticateTenantAdminWithCsrf();
        UUID operatorId = seedOperator(companyAId, "Ana", "ana", PermissionLevel.BASIC, true).getId();

        Response response = LocalHttpTestClient.delete(
                "http://localhost:" + port + "/api/operators/" + operatorId,
                Map.of(
                        "Cookie", "kalles_auth_token=" + auth.authCookie() + "; XSRF-TOKEN=" + auth.csrfCookie(),
                        "X-Company-ID", companyAId.toString(),
                        "X-XSRF-TOKEN", auth.csrfToken(),
                        "Content-Type", "application/json"
                )
        );

        response.then()
                .statusCode(204);

        assertThat(operatorRepository.findById(operatorId)).isPresent();
        assertThat(operatorRepository.findById(operatorId).orElseThrow().isActive()).isFalse();
    }
}
