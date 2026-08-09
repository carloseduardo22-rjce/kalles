package dev.kalles.goal.integration;

import dev.kalles.goal.enums.GoalStatus;
import dev.kalles.goal.enums.Periodicity;
import dev.kalles.goal.support.AbstractGoalApiSupport;
import dev.kalles.payment.support.LocalHttpTestClient;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@Tag("integration")
class GoalApiIntegrationTest extends AbstractGoalApiSupport {

    @BeforeEach
    void setUp() {
        resetGoalScenario();
    }

    @Test
    void shouldCreateGoalInsideActiveCompany() {
        AuthContext auth = authenticateTenantAdminWithCsrf();

        givenAuthenticated(auth, companyAId)
                .body(Map.of(
                        "targetValue", 10000.00,
                        "periodicity", "MONTHLY",
                        "startDate", "2026-04-01",
                        "endDate", "2026-04-30"
                ))
                .when()
                .post("/api/goals")
                .then()
                .statusCode(201)
                .body("targetValue", equalTo(10000.00f))
                .body("status", equalTo("DRAFT"));
    }

    @Test
    void shouldRejectOverlappingActiveGoalWithinSameCompany() {
        AuthContext auth = authenticateTenantAdminWithCsrf();
        seedGoal(companyAId, "9000.00", Periodicity.MONTHLY, LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 25), GoalStatus.ACTIVE);

        givenAuthenticated(auth, companyAId)
                .body(Map.of(
                        "targetValue", 10000.00,
                        "periodicity", "MONTHLY",
                        "startDate", "2026-04-01",
                        "endDate", "2026-04-30"
                ))
                .when()
                .post("/api/goals")
                .then()
                .statusCode(422);
    }

    @Test
    void shouldAllowOverlappingGoalInDifferentCompanies() {
        AuthContext auth = authenticateTenantAdminWithCsrf();
        seedGoal(companyBId, "9000.00", Periodicity.MONTHLY, LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 25), GoalStatus.ACTIVE);

        givenAuthenticated(auth, companyAId)
                .body(Map.of(
                        "targetValue", 10000.00,
                        "periodicity", "MONTHLY",
                        "startDate", "2026-04-01",
                        "endDate", "2026-04-30"
                ))
                .when()
                .post("/api/goals")
                .then()
                .statusCode(201)
                .body("status", equalTo("DRAFT"));
    }

    @Test
    void shouldListOnlyGoalsFromActiveCompany() {
        AuthContext auth = authenticateTenantAdminWithCsrf();
        seedGoal(companyAId, "10000.00", Periodicity.MONTHLY, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), GoalStatus.DRAFT);
        seedGoal(companyBId, "12000.00", Periodicity.MONTHLY, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), GoalStatus.ACTIVE);

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/goals",
                Map.of(
                        "Cookie", "kalles_auth_token=" + auth.authCookie(),
                        "X-Company-ID", companyAId.toString()
                )
        );

        response.then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].status", equalTo("DRAFT"));
    }

    @Test
    void shouldReturnNotFoundWhenFetchingGoalFromAnotherCompany() {
        AuthContext auth = authenticateTenantAdminWithCsrf();
        UUID foreignGoalId = seedGoal(companyBId, "12000.00", Periodicity.MONTHLY, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), GoalStatus.ACTIVE).getId();

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/goals/" + foreignGoalId,
                Map.of(
                        "Cookie", "kalles_auth_token=" + auth.authCookie(),
                        "X-Company-ID", companyAId.toString()
                )
        );

        response.then()
                .statusCode(404)
                .body("detail", equalTo("Meta não encontrada: " + foreignGoalId));
    }

    @Test
    void shouldRequireActiveCompanyForGoalRoutes() {
        String authCookie = loginAndExtractAuthCookie(TENANT_ADMIN_EMAIL);

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/goals",
                Map.of("Cookie", "kalles_auth_token=" + authCookie)
        );

        response.then()
                .statusCode(400)
                .body("code", equalTo("COMPANY_CONTEXT_REQUIRED"));
    }
}
