package dev.kalles.sale.security.integration;

import dev.kalles.sale.KallesSaleApplication;
import dev.kalles.sale.cashregister.entity.CashRegister;
import dev.kalles.sale.cashregister.repository.CashRegisterRepository;
import dev.kalles.sale.core.entity.Company;
import dev.kalles.sale.core.entity.Tenant;
import dev.kalles.sale.core.repository.CompanyRepository;
import dev.kalles.sale.core.repository.TenantRepository;
import dev.kalles.sale.security.domain.Account;
import dev.kalles.sale.security.domain.AccountRole;
import dev.kalles.sale.security.domain.PosDeviceSession;
import dev.kalles.sale.security.repository.AccountRepository;
import dev.kalles.sale.security.repository.PosDeviceSessionRepository;
import dev.kalles.sale.security.support.AbstractSecurityApiContainerSupport;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

@Disabled("Spec-first contract for auth and POS pairing. Enable after Etapa 3 implementation.")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KallesSaleApplication.class)
class AuthAndPosSetupApiIntegrationTest extends AbstractSecurityApiContainerSupport {

    private static final UUID TENANT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID COMPANY_ID = UUID.fromString("e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f");

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

    private UUID cashRegisterId;

    @BeforeEach
    void setUp() {
        RestAssured.reset();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        posDeviceSessionRepository.deleteAll();
        accountRepository.deleteAll();
        cashRegisterRepository.deleteAll();
        companyRepository.deleteAll();
        tenantRepository.deleteAll();

        tenantRepository.save(new Tenant(TENANT_ID, "Conta de Teste Kalles"));
        companyRepository.save(new Company(
                COMPANY_ID,
                "Loja Matriz",
                TENANT_ID,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        cashRegisterId = cashRegisterRepository.save(
                new CashRegister("CAIXA-01", "Caixa principal", COMPANY_ID)
        ).getId();

        accountRepository.save(newAccount(
                "Administrador",
                "admin@sistema.local",
                AccountRole.ADMIN,
                COMPANY_ID
        ));

        accountRepository.save(newAccount(
                "Operador Caixa 01",
                "operador.caixa01@sistema.local",
                AccountRole.OPERATOR,
                COMPANY_ID
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

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("kalles_auth_token", authCookie)
                .body(Map.of(
                        "companyId", COMPANY_ID,
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
        String pairingToken = seedPairingToken("pairing-token-caixa-01", COMPANY_ID, cashRegisterId, true);

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
        String pairingToken = seedPairingToken("pairing-token-valido", COMPANY_ID, cashRegisterId, true);

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
        String pairingToken = seedPairingToken("pairing-token-revogado", COMPANY_ID, cashRegisterId, false);

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

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("kalles_auth_token", authCookie)
                .body(Map.of())
                .when()
                .post("/api/pos/admin/generate-token")
                .then()
                .statusCode(400);
    }

    private Account newAccount(String name, String email, AccountRole role, UUID companyId) {
        Account account = new Account(TENANT_ID, name, email, passwordEncoder.encode("123456"), role);
        account.setId(UUID.randomUUID());
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
