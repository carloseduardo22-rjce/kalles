package dev.kalles.security.integration;

import dev.kalles.testsupport.LocalHttpTestClient;
import dev.kalles.security.support.AbstractCompanyContextApiSupport;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

class CompanyContextApiIntegrationTest extends AbstractCompanyContextApiSupport {

    @BeforeEach
    void setUp() {
        resetScenarioData();
    }

    @Test
    void shouldRequireCompanyHeaderForScopedRouteWhenAdminHasNoFixedCompany() {
        String authCookie = loginAndExtractAuthCookie(TENANT_ADMIN_EMAIL);

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/cash-registers",
                java.util.Map.of("Cookie", "kalles_auth_token=" + authCookie)
        );

        response.then()
                .statusCode(400)
                .body("code", equalTo("COMPANY_CONTEXT_REQUIRED"));
    }

    @Test
    void shouldAllowScopedRouteWhenTenantAdminProvidesAccessibleCompanyHeader() {
        String authCookie = loginAndExtractAuthCookie(TENANT_ADMIN_EMAIL);

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/cash-registers",
                java.util.Map.of(
                        "Cookie", "kalles_auth_token=" + authCookie,
                        "X-Company-ID", companyBId.toString()
                )
        );

        response.then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].code", equalTo("CX-B1"));
    }

    @Test
    void shouldRejectForeignCompanyHeaderForTenantAdmin() {
        String authCookie = loginAndExtractAuthCookie(TENANT_ADMIN_EMAIL);

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/cash-registers",
                java.util.Map.of(
                        "Cookie", "kalles_auth_token=" + authCookie,
                        "X-Company-ID", foreignCompanyId.toString()
                )
        );

        response.then()
                .statusCode(403)
                .body("code", equalTo("COMPANY_CONTEXT_DENIED"));
    }

    @Test
    void shouldRejectConflictingCompanyHeaderForBoundAdmin() {
        String authCookie = loginAndExtractAuthCookie(BOUND_ADMIN_EMAIL);

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/cash-registers",
                java.util.Map.of(
                        "Cookie", "kalles_auth_token=" + authCookie,
                        "X-Company-ID", companyBId.toString()
                )
        );

        response.then()
                .statusCode(403)
                .body("code", equalTo("COMPANY_CONTEXT_DENIED"));
    }

    @Test
    void shouldRestrictCompanyListingToBoundCompanyWhenTokenHasFixedCompany() {
        String authCookie = loginAndExtractAuthCookie(BOUND_ADMIN_EMAIL);

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/companies",
                java.util.Map.of("Cookie", "kalles_auth_token=" + authCookie)
        );

        response.then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].id", equalTo(companyAId.toString()));
    }
}
