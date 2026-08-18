package dev.kalles.operator.support;

import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.enums.PermissionLevel;
import dev.kalles.cashregister.repository.OperatorRepository;
import dev.kalles.security.support.AbstractCompanyContextApiSupport;
import dev.kalles.testsupport.CsrfTestClient;
import dev.kalles.testsupport.CsrfTestClient.CsrfContext;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static io.restassured.RestAssured.given;

public abstract class AbstractOperatorApiSupport extends AbstractCompanyContextApiSupport {

    @Autowired
    protected OperatorRepository operatorRepository;

    protected void resetOperatorScenario() {
        operatorRepository.deleteAll();
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

    protected Operator seedOperator(UUID companyId, String name, String code, PermissionLevel permissionLevel, boolean active) {
        Operator operator = new Operator();
        operator.setCompanyId(companyId);
        operator.setName(name);
        operator.setCode(code);
        operator.setPermissionLevel(permissionLevel);
        operator.setActive(active);
        return operatorRepository.save(operator);
    }

    protected record AuthContext(String authCookie, String csrfCookie, String csrfToken) {
    }
}
