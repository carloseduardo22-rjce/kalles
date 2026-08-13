package dev.kalles.security.integration;

import dev.kalles.KallesSaleApplication;
import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.repository.CashRegisterRepository;
import dev.kalles.company.entity.Company;
import dev.kalles.company.entity.Tenant;
import dev.kalles.company.repository.CompanyRepository;
import dev.kalles.company.repository.TenantRepository;
import dev.kalles.security.entity.Account;
import dev.kalles.security.entity.PosDeviceSession;
import dev.kalles.security.enums.AccountRole;
import dev.kalles.security.repository.AccountRepository;
import dev.kalles.security.repository.PosDeviceSessionRepository;
import dev.kalles.security.support.AbstractSecurityApiContainerSupport;
import dev.kalles.testsupport.CsrfTestClient;
import dev.kalles.testsupport.CsrfTestClient.CsrfContext;
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

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
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
    private PosDeviceSessionRepository posDeviceSessionRepository;

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
    void shouldBlockOperatorLoginWithoutPosToken() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "operador.caixa01@sistema.local",
                        "password", "123456"
                ))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(400)
                .body("detail", equalTo("Terminal não configurado. Por favor, solicite o pareamento do caixa."));
    }

    @Test
    void shouldGeneratePairingTokenForAuthenticatedAdmin() {
        String authCookie = loginAndExtractAuthCookie("admin@sistema.local", "123456");
        CsrfContext csrf = fetchCsrfToken();

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("kalles_auth_token", authCookie)
                .cookie("XSRF-TOKEN", csrf.csrfCookie())
                .header("X-XSRF-TOKEN", csrf.csrfToken())
                .body(Map.of(
                        "companyId", companyId,
                        "posId", cashRegisterId
                ))
                .when()
                .post("/api/pos/admin/generate-token");

        response.then()
                .statusCode(200)
                .body("pairingToken", not(blankOrNullString()));

        String pairingToken = response.jsonPath().getString("pairingToken");
        assertThat(posDeviceSessionRepository.findByTokenAndActiveTrueAndExpiresAtGreaterThan(
                pairingToken,
                LocalDateTime.now().minusSeconds(1)
        )).isPresent();
    }

    @Test
    void shouldSetupDeviceCookieWithValidPairingToken() {
        String pairingToken = seedPairingToken("pairing-token-caixa-01", companyId, cashRegisterId, true);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("pairingToken", pairingToken))
                .when()
                .post("/api/pos/setup")
                .then()
                .statusCode(200)
                .body("message", equalTo("Dispositivo pareado com sucesso."))
                .header(HttpHeaders.SET_COOKIE, not(blankOrNullString()));
    }

    @Test
    void shouldAllowOperatorLoginWithValidPosToken() {
        String pairingToken = seedPairingToken("pairing-token-valido", companyId, cashRegisterId, true);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("kalles_pos_token", pairingToken)
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
    void shouldBlockOperatorLoginWithRevokedPosToken() {
        String pairingToken = seedPairingToken("pairing-token-revogado", companyId, cashRegisterId, false);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("kalles_pos_token", pairingToken)
                .body(Map.of(
                        "email", "operador.caixa01@sistema.local",
                        "password", "123456"
                ))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(400)
                .body("detail", equalTo("Sessão do terminal inválida ou expirada."));
    }

    @Test
    void shouldRejectInvalidPairingToken() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("pairingToken", "token-invalido"))
                .when()
                .post("/api/pos/setup")
                .then()
                .statusCode(400)
                .body("detail", equalTo("Token de pareamento inválido ou expirado."));
    }

    @Test
    void shouldRequireCompanyIdAndPosIdWhenGeneratingPairingToken() {
        String authCookie = loginAndExtractAuthCookie("admin@sistema.local", "123456");
        CsrfContext csrf = fetchCsrfToken();

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("kalles_auth_token", authCookie)
                .cookie("XSRF-TOKEN", csrf.csrfCookie())
                .header("X-XSRF-TOKEN", csrf.csrfToken())
                .body(Map.of())
                .when()
                .post("/api/pos/admin/generate-token")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldRejectPairingTokenGenerationForOperator() {
        String pairingToken = seedPairingToken("pairing-token-operador", companyId, cashRegisterId, true);
        String authCookie = loginOperatorAndExtractAuthCookie(pairingToken);
        CsrfContext csrf = fetchCsrfToken();

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("kalles_auth_token", authCookie)
                .cookie("XSRF-TOKEN", csrf.csrfCookie())
                .header("X-XSRF-TOKEN", csrf.csrfToken())
                .body(Map.of(
                        "companyId", companyId,
                        "posId", cashRegisterId
                ))
                .when()
                .post("/api/pos/admin/generate-token")
                .then()
                .statusCode(403);

        assertThat(posDeviceSessionRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldRejectPairingTokenGenerationForCompanyOfAnotherTenant() {
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

        String authCookie = loginAndExtractAuthCookie("admin@sistema.local", "123456");
        CsrfContext csrf = fetchCsrfToken();

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("kalles_auth_token", authCookie)
                .cookie("XSRF-TOKEN", csrf.csrfCookie())
                .header("X-XSRF-TOKEN", csrf.csrfToken())
                .body(Map.of(
                        "companyId", otherCompanyId,
                        "posId", cashRegisterId
                ))
                .when()
                .post("/api/pos/admin/generate-token")
                .then()
                .statusCode(403);

        assertThat(posDeviceSessionRepository.count()).isZero();
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
                    .statusCode(400);
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

    private CsrfContext fetchCsrfToken() {
        return CsrfTestClient.fetch(port);
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

    private String loginOperatorAndExtractAuthCookie(String pairingToken) {
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("kalles_pos_token", pairingToken)
                .body(Map.of("email", "operador.caixa01@sistema.local", "password", "123456"))
                .when()
                .post("/api/auth/login");

        response.then().statusCode(200);
        return response.getCookie("kalles_auth_token");
    }

    private String seedPairingToken(String token, UUID companyId, UUID posId, boolean active) {
        PosDeviceSession session = new PosDeviceSession();
        session.setCompanyId(companyId);
        session.setPosId(posId);
        session.setToken(token);
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusDays(30));
        session.setActive(active);
        posDeviceSessionRepository.save(session);
        return token;
    }
}
