package dev.kalles.security.integration;

import dev.kalles.KallesSaleApplication;
import dev.kalles.company.entity.Company;
import dev.kalles.company.entity.Tenant;
import dev.kalles.company.repository.CompanyRepository;
import dev.kalles.company.repository.TenantRepository;
import dev.kalles.security.entity.Account;
import dev.kalles.security.enums.AccountRole;
import dev.kalles.security.repository.AccountRepository;
import dev.kalles.security.repository.RefreshTokenSessionRepository;
import dev.kalles.security.support.AbstractSecurityApiContainerSupport;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KallesSaleApplication.class)
class RefreshTokenFlowIntegrationTest extends AbstractSecurityApiContainerSupport {

    private static final UUID TENANT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174111");

    @LocalServerPort
    private int port;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RefreshTokenSessionRepository refreshTokenSessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        RestAssured.reset();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        databaseCleaner().clean();

        tenantRepository.save(new Tenant(TENANT_ID, "Tenant Refresh"));
        Company company = companyRepository.save(new Company(
                null,
                "Loja Refresh",
                TENANT_ID,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        Account account = new Account(TENANT_ID, "Administrador", "refresh@sistema.local", passwordEncoder.encode("123456"), AccountRole.ADMIN);
        account.setCompanyId(company.getId());
        account.setVerified(true);
        accountRepository.save(account);
    }

    @Test
    void shouldIssueRefreshCookieOnLoginAndRotateOnRefresh() {
        Response loginResponse = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "refresh@sistema.local", "password", "123456"))
                .when()
                .post("/api/auth/login");

        loginResponse.then().statusCode(200);

        String accessCookie = loginResponse.getCookie("kalles_auth_token");
        String refreshCookie = loginResponse.getCookie("kalles_refresh_token");

        assertThat(accessCookie).isNotBlank();
        assertThat(refreshCookie).isNotBlank();
        assertThat(refreshTokenSessionRepository.findAll()).hasSize(1);

        Response refreshResponse = RestAssured.given()
                .cookie("kalles_refresh_token", refreshCookie)
                .when()
                .post("/api/auth/refresh");

        refreshResponse.then().statusCode(200);

        String rotatedAccessCookie = refreshResponse.getCookie("kalles_auth_token");
        String rotatedRefreshCookie = refreshResponse.getCookie("kalles_refresh_token");

        assertThat(rotatedAccessCookie).isNotBlank();
        assertThat(rotatedRefreshCookie).isNotBlank();
        assertThat(rotatedRefreshCookie).isNotEqualTo(refreshCookie);
    }

    @Test
    void shouldRejectInvalidRefreshToken() {
        Response response = RestAssured.given()
                .cookie("kalles_refresh_token", "refresh-invalido")
                .when()
                .post("/api/auth/refresh");

        response.then().statusCode(401);
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).isNull();
    }

    @Test
    void shouldRevokeRefreshTokenOnLogout() {
        Response loginResponse = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "refresh@sistema.local", "password", "123456"))
                .when()
                .post("/api/auth/login");

        String refreshCookie = loginResponse.getCookie("kalles_refresh_token");

        Response logoutResponse = RestAssured.given()
                .cookie("kalles_refresh_token", refreshCookie)
                .when()
                .post("/api/auth/logout");

        logoutResponse.then().statusCode(200);

        Response refreshResponse = RestAssured.given()
                .cookie("kalles_refresh_token", refreshCookie)
                .when()
                .post("/api/auth/refresh");

        refreshResponse.then().statusCode(401);
    }
}
