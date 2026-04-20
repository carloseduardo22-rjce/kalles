package dev.kalles.sale.fidelity.integration;

import dev.kalles.sale.core.enums.fidelity.FidelityDiscountType;
import dev.kalles.sale.fidelity.support.AbstractFidelityPolicyApiSupport;
import dev.kalles.sale.payment.support.LocalHttpTestClient;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@Tag("integration")
class FidelityPolicyApiIntegrationTest extends AbstractFidelityPolicyApiSupport {

    @BeforeEach
    void setUp() {
        resetFidelityPolicyScenario();
    }

    @Test
    void shouldCreatePolicyInsideActiveCompanyAndDeactivatePreviousOne() {
        AuthContext auth = authenticateTenantAdminWithCsrf();
        var previousPolicy = seedPolicy(
                companyAId, 100, "20.00", 1, FidelityDiscountType.FIXED, true, LocalDate.now().minusDays(1)
        );

        givenAuthenticated(auth, companyAId)
                .body(Map.of(
                        "objectivePoints", 200,
                        "configuredDiscount", 15.00,
                        "valuePoint", 2,
                        "discountType", "PERCENTAGE"
                ))
                .when()
                .post("/api/fidelity-policies")
                .then()
                .statusCode(201)
                .body("objectivePoints", equalTo(200))
                .body("discountType", equalTo("PERCENTAGE"))
                .body("active", equalTo(true));

        assertThat(fidelityPolicyRepository.findById(previousPolicy.getId())).isPresent();
        assertThat(fidelityPolicyRepository.findById(previousPolicy.getId()).orElseThrow().isActive()).isFalse();
        assertThat(fidelityPolicyRepository.findFirstByCompanyIdAndActiveTrue(companyAId)).isPresent();
        assertThat(fidelityPolicyRepository.findFirstByCompanyIdAndActiveTrue(companyAId).orElseThrow().getObjectivePoints())
                .isEqualTo(200);
    }

    @Test
    void shouldReturnOnlyActivePolicyFromActiveCompany() {
        AuthContext auth = authenticateTenantAdminWithCsrf();
        seedPolicy(companyAId, 100, "20.00", 1, FidelityDiscountType.FIXED, true, LocalDate.now());
        seedPolicy(companyBId, 300, "30.00", 3, FidelityDiscountType.PERCENTAGE, true, LocalDate.now());

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/fidelity-policies/active",
                Map.of(
                        "Cookie", "kalles_auth_token=" + auth.authCookie(),
                        "X-Company-ID", companyAId.toString()
                )
        );

        response.then()
                .statusCode(200)
                .body("objectivePoints", equalTo(100))
                .body("discountType", equalTo("FIXED"));
    }

    @Test
    void shouldListHistoryOnlyFromActiveCompany() {
        AuthContext auth = authenticateTenantAdminWithCsrf();
        seedPolicy(companyAId, 100, "20.00", 1, FidelityDiscountType.FIXED, false, LocalDate.now().minusDays(2));
        seedPolicy(companyAId, 200, "15.00", 2, FidelityDiscountType.PERCENTAGE, true, LocalDate.now().minusDays(1));
        seedPolicy(companyBId, 300, "30.00", 3, FidelityDiscountType.FIXED, true, LocalDate.now());

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/fidelity-policies",
                Map.of(
                        "Cookie", "kalles_auth_token=" + auth.authCookie(),
                        "X-Company-ID", companyAId.toString()
                )
        );

        response.then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("[0].objectivePoints", equalTo(200))
                .body("[1].objectivePoints", equalTo(100));
    }

    @Test
    void shouldRejectPercentageAboveOneHundred() {
        AuthContext auth = authenticateTenantAdminWithCsrf();

        givenAuthenticated(auth, companyAId)
                .body(Map.of(
                        "objectivePoints", 200,
                        "configuredDiscount", 150.00,
                        "valuePoint", 2,
                        "discountType", "PERCENTAGE"
                ))
                .when()
                .post("/api/fidelity-policies")
                .then()
                .statusCode(400)
                .body("detail", equalTo("O desconto percentual nao pode ultrapassar 100%."));
    }

    @Test
    void shouldRequireActiveCompanyForFidelityPolicyRoutes() {
        String authCookie = loginAndExtractAuthCookie(TENANT_ADMIN_EMAIL);

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/fidelity-policies",
                Map.of("Cookie", "kalles_auth_token=" + authCookie)
        );

        response.then()
                .statusCode(400)
                .body("code", equalTo("COMPANY_CONTEXT_REQUIRED"));
    }
}
