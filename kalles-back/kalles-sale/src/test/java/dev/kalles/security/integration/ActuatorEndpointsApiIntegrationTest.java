package dev.kalles.security.integration;

import dev.kalles.security.support.AbstractCompanyContextApiSupport;
import dev.kalles.testsupport.LocalHttpTestClient;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

class ActuatorEndpointsApiIntegrationTest extends AbstractCompanyContextApiSupport {

    @BeforeEach
    void setUp() {
        resetScenarioData();
    }

    private Response get(String path) {
        return LocalHttpTestClient.get("http://localhost:" + port + path, Map.of());
    }

    private Response getAsAdmin(String path) {
        String authCookie = loginAndExtractAuthCookie(TENANT_ADMIN_EMAIL);
        return LocalHttpTestClient.get(
                "http://localhost:" + port + path,
                Map.of("Cookie", "kalles_auth_token=" + authCookie)
        );
    }

    @Test
    void shouldAnswerHealthWithoutAuthentication() {
        get("/actuator/health").then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test
    void shouldAnswerTheReadinessProbe() {
        get("/actuator/health/readiness").then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test
    void shouldHideTheHealthDetailsFromAnUnauthenticatedCaller() {
        get("/actuator/health").then()
                .statusCode(200)
                .body("components", nullValue());
    }

    @Test
    void shouldStayUpWhileTheMailServerIsUnreachable() {
        get("/actuator/health").then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test
    void shouldNotExposeTheEnvironmentEvenToAnAdmin() {
        getAsAdmin("/actuator/env").then().statusCode(404);
    }

    @Test
    void shouldNotExposeTheConfigurationPropertiesEvenToAnAdmin() {
        getAsAdmin("/actuator/configprops").then().statusCode(404);
    }

    @Test
    void shouldNotExposeTheBeansEvenToAnAdmin() {
        getAsAdmin("/actuator/beans").then().statusCode(404);
    }
}
