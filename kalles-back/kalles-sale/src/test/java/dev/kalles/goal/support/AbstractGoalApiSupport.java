package dev.kalles.goal.support;

import dev.kalles.goal.entity.Goal;
import dev.kalles.goal.enums.GoalStatus;
import dev.kalles.goal.enums.Periodicity;
import dev.kalles.goal.repository.GoalRepository;
import dev.kalles.security.support.AbstractCompanyContextApiSupport;
import dev.kalles.testsupport.CsrfTestClient;
import dev.kalles.testsupport.CsrfTestClient.CsrfContext;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public abstract class AbstractGoalApiSupport extends AbstractCompanyContextApiSupport {

    @Autowired
    protected GoalRepository goalRepository;

    protected void resetGoalScenario() {
        goalRepository.deleteAll();
        resetScenarioData();
        RestAssured.reset();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    protected AuthContext authenticateTenantAdminWithCsrf() {
        String authCookie = loginAndExtractAuthCookie(TENANT_ADMIN_EMAIL);
        CsrfContext csrf = CsrfTestClient.fetch(port);
        return new AuthContext(authCookie, csrf.csrfCookie(), csrf.csrfToken());
    }

    protected RequestSpecification givenAuthenticated(AuthContext authContext, UUID companyId) {
        return given()
                .cookie("kalles_auth_token", authContext.authCookie())
                .cookie("XSRF-TOKEN", authContext.csrfCookie())
                .header("X-XSRF-TOKEN", authContext.csrfToken())
                .header("X-Company-ID", companyId.toString())
                .contentType(ContentType.JSON);
    }

    protected Goal seedGoal(UUID companyId, String targetValue, Periodicity periodicity, LocalDate startDate, LocalDate endDate, GoalStatus status) {
        Goal goal = Goal.create(companyId, new BigDecimal(targetValue), periodicity, startDate, endDate);
        if (status == GoalStatus.ACTIVE) {
            goal.activate();
        } else if (status == GoalStatus.CLOSED) {
            goal.close();
        }
        return goalRepository.save(goal);
    }

    protected record AuthContext(String authCookie, String csrfCookie, String csrfToken) {
    }

}
