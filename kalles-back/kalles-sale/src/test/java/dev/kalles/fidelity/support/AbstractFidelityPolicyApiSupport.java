package dev.kalles.fidelity.support;

import dev.kalles.fidelity.entity.FidelityPolicy;
import dev.kalles.fidelity.enums.FidelityDiscountType;
import dev.kalles.fidelity.repository.FidelityPolicyRepository;
import dev.kalles.security.support.AbstractCompanyContextApiSupport;
import dev.kalles.testsupport.CsrfTestClient;
import dev.kalles.testsupport.CsrfTestClient.CsrfContext;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public abstract class AbstractFidelityPolicyApiSupport extends AbstractCompanyContextApiSupport {

    @Autowired
    protected FidelityPolicyRepository fidelityPolicyRepository;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected void resetFidelityPolicyScenario() {
        fidelityPolicyRepository.deleteAll();
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

    protected FidelityPolicy seedPolicy(
            UUID companyId,
            int objectivePoints,
            String configuredDiscount,
            int valuePoint,
            FidelityDiscountType discountType,
            boolean active,
            LocalDateTime createdAt
    ) {
        FidelityPolicy policy = new FidelityPolicy();
        policy.setCompanyId(companyId);
        policy.setObjectivePoints(objectivePoints);
        policy.setConfiguredDiscount(new BigDecimal(configuredDiscount));
        policy.setValuePoint(valuePoint);
        policy.setDiscountType(discountType);
        policy.setActive(active);
        FidelityPolicy saved = fidelityPolicyRepository.save(policy);
        jdbcTemplate.update("UPDATE fidelity_policy SET created_at = ? WHERE id = ?", createdAt, saved.getId());
        saved.setCreatedAt(createdAt);
        return saved;
    }

    protected record AuthContext(String authCookie, String csrfCookie, String csrfToken) {
    }

}
