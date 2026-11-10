package dev.kalles.security.integration;

import dev.kalles.KallesSaleApplication;
import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.repository.CashRegisterRepository;
import dev.kalles.company.entity.Company;
import dev.kalles.company.entity.Tenant;
import dev.kalles.company.repository.CompanyRepository;
import dev.kalles.company.repository.TenantRepository;
import dev.kalles.security.entity.Account;
import dev.kalles.security.enums.AccountRole;
import dev.kalles.security.repository.AccountRepository;
import dev.kalles.security.repository.PosPairingCodeRepository;
import dev.kalles.security.support.AbstractSecurityApiContainerSupport;
import dev.kalles.testsupport.CsrfTestClient;
import dev.kalles.testsupport.CsrfTestClient.CsrfContext;
import dev.kalles.testsupport.LocalHttpTestClient;
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.blankOrNullString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KallesSaleApplication.class)
class AuthAndPosSetupApiIntegrationTest extends AbstractSecurityApiContainerSupport {

    private static final UUID TENANT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final String RATE_LIMITED_EMAIL = "alvo.rate.limit@sistema.local";

    @LocalServerPort
    private int port;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CashRegisterRepository cashRegisterRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PosPairingCodeRepository posPairingCodeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UUID companyId;
    private UUID cashRegisterId;

    @BeforeEach
    void setUp() {
        RestAssured.reset();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        databaseCleaner().clean();

        tenantRepository.save(new Tenant(TENANT_ID, "Conta de Teste Kalles"));
        companyId = companyRepository.save(new Company(
                null,
                "Loja Matriz",
                TENANT_ID,
                null,
                null,
                null,
                null,
                null,
                null
        )).getId();

        cashRegisterId = cashRegisterRepository.save(
                new CashRegister("CAIXA-01", "Caixa principal", companyId)
        ).getId();

        accountRepository.save(newAccount(
                "Administrador",
                "admin@sistema.local",
                AccountRole.ADMIN,
                companyId
        ));

        accountRepository.save(newAccount(
                "Operador Caixa 01",
                "operador.caixa01@sistema.local",
                AccountRole.OPERATOR,
                companyId
        ));
    }

    @Test
    void shouldAllowAdminLoginWithoutPosToken() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "admin@sistema.local",
                        "password", "123456"
                ))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .header(HttpHeaders.SET_COOKIE, not(blankOrNullString()));
    }

    @Test
    void shouldAllowOperatorLoginWithoutPairedDevice() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "operador.caixa01@sistema.local",
                        "password", "123456"
                ))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .header(HttpHeaders.SET_COOKIE, not(blankOrNullString()));
    }

    @Test
    void shouldRejectPairingCodeGenerationForOperator() {
        String authCookie = loginAndExtractAuthCookie("operador.caixa01@sistema.local", "123456");
        CsrfContext csrf = CsrfTestClient.fetch(port);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("kalles_auth_token", authCookie)
                .cookie("XSRF-TOKEN", csrf.csrfCookie())
                .header("X-XSRF-TOKEN", csrf.csrfToken())
                .when()
                .post("/api/cash-registers/{id}/pairing-code", cashRegisterId)
                .then()
                .statusCode(403);

        assertThat(posPairingCodeRepository.count()).isZero();
    }

    @Test
    void shouldNotGeneratePairingCodeForCashRegisterOfAnotherTenant() {
        UUID otherTenantId = UUID.fromString("99999999-e89b-12d3-a456-426614174000");
        tenantRepository.save(new Tenant(otherTenantId, "Conta de Outro Cliente"));
        UUID otherCompanyId = companyRepository.save(new Company(
                null,
                "Loja de Outro Cliente",
                otherTenantId,
                null,
                null,
                null,
                null,
                null,
                null
        )).getId();
        UUID otherCashRegisterId = cashRegisterRepository.save(
                new CashRegister("CAIXA-99", "Caixa de outro cliente", otherCompanyId)
        ).getId();

        String authCookie = loginAndExtractAuthCookie("admin@sistema.local", "123456");
        CsrfContext csrf = CsrfTestClient.fetch(port);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("kalles_auth_token", authCookie)
                .cookie("XSRF-TOKEN", csrf.csrfCookie())
                .header("X-XSRF-TOKEN", csrf.csrfToken())
                .when()
                .post("/api/cash-registers/{id}/pairing-code", otherCashRegisterId)
                .then()
                .statusCode(404)
                .body("code", equalTo("CASH_REGISTER_NOT_FOUND"));

        assertThat(posPairingCodeRepository.count()).isZero();
    }

    @Test
    void shouldAnswerProtectedRouteWithoutSessionWithUnauthorizedProblem() {
        LocalHttpTestClient.get("http://localhost:" + port + "/api/auth/me", Map.of())
                .then()
                .statusCode(401)
                .contentType("application/problem+json")
                .body("code", equalTo("AUTHENTICATION_REQUIRED"))
                .body("path", equalTo("/api/auth/me"));
    }

    @Test
    void shouldAnswerProtectedRouteWithInvalidTokenWithUnauthorizedProblem() {
        LocalHttpTestClient.get(
                        "http://localhost:" + port + "/api/auth/me",
                        Map.of("Cookie", "kalles_auth_token=token-invalido"))
                .then()
                .statusCode(401)
                .body("code", equalTo("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void shouldAcceptEveryConfiguredOrigin() {
        for (String origin : new String[] {"http://localhost:3000", "http://localhost:3001"}) {
            RestAssured.given()
                    .header(HttpHeaders.ORIGIN, origin)
                    .contentType(ContentType.JSON)
                    .body(Map.of("email", "admin@sistema.local", "password", "123456"))
                    .when()
                    .post("/api/auth/login")
                    .then()
                    .statusCode(200)
                    .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        }
    }

    @Test
    void shouldRejectOriginOutsideTheConfiguredList() {
        RestAssured.given()
                .header(HttpHeaders.ORIGIN, "http://localhost:4000")
                .contentType(ContentType.JSON)
                .body(Map.of("email", "admin@sistema.local", "password", "123456"))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(403)
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, nullValue());
    }

    @Test
    void shouldAnswerWrongPasswordAndUnknownAccountWithTheSameUnauthorized() {
        for (String email : new String[] {"admin@sistema.local", "ninguem@sistema.local"}) {
            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("email", email, "password", "senha-errada"))
                    .when()
                    .post("/api/auth/login")
                    .then()
                    .statusCode(401)
                    .body("code", equalTo("INVALID_CREDENTIALS"))
                    .body("detail", equalTo("Credenciais invalidas."));
        }
    }

    @Test
    void shouldReturnTooManyRequestsAfterRepeatedLoginFailures() {
        accountRepository.save(newAccount("Alvo do rate limit", RATE_LIMITED_EMAIL, AccountRole.ADMIN, companyId));

        for (int attempt = 0; attempt < 5; attempt++) {
            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("email", RATE_LIMITED_EMAIL, "password", "senha-errada"))
                    .when()
                    .post("/api/auth/login")
                    .then()
                    .statusCode(401);
        }

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", RATE_LIMITED_EMAIL, "password", "senha-errada"))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(429)
                .body("title", equalTo("Muitas tentativas"))
                .body("detail", equalTo("Muitas tentativas de login. Aguarde alguns minutos antes de tentar novamente."));
    }

    private Account newAccount(String name, String email, AccountRole role, UUID companyId) {
        Account account = new Account(TENANT_ID, name, email, passwordEncoder.encode("123456"), role);
        account.setCompanyId(companyId);
        account.setVerified(true);
        return account;
    }

    private String loginAndExtractAuthCookie(String email, String password) {
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", password))
                .when()
                .post("/api/auth/login");

        response.then().statusCode(200);
        return response.getCookie("kalles_auth_token");
    }
}
