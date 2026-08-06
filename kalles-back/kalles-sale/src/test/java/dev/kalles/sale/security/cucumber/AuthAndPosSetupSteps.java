package dev.kalles.sale.security.cucumber;

import com.auth0.jwt.interfaces.DecodedJWT;
import dev.kalles.sale.cashregister.entity.CashRegister;
import dev.kalles.sale.cashregister.entity.Operator;
import dev.kalles.sale.cashregister.repository.CashRegisterRepository;
import dev.kalles.sale.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.sale.cashregister.repository.OperatorRepository;
import dev.kalles.sale.core.entity.Company;
import dev.kalles.sale.core.entity.Tenant;
import dev.kalles.sale.core.enums.operator.PermissionLevel;
import dev.kalles.sale.core.repository.CompanyRepository;
import dev.kalles.sale.core.repository.TenantRepository;
import dev.kalles.sale.security.domain.Account;
import dev.kalles.sale.security.domain.AccountRole;
import dev.kalles.sale.security.domain.PosDeviceSession;
import dev.kalles.sale.security.repository.AccountRepository;
import dev.kalles.sale.security.repository.PosDeviceSessionRepository;
import dev.kalles.sale.security.service.JwtService;
import dev.kalles.sale.testsupport.CsrfTestClient;
import dev.kalles.sale.testsupport.CsrfTestClient.CsrfContext;
import dev.kalles.sale.testsupport.DatabaseCleaner;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthAndPosSetupSteps {

    private static final UUID TENANT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @LocalServerPort
    private int port;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CashRegisterRepository cashRegisterRepository;

    @Autowired
    private CashRegisterSessionRepository cashRegisterSessionRepository;

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PosDeviceSessionRepository posDeviceSessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    private Response response;
    private UUID companyId;
    private UUID cashRegisterId;
    private String currentPosCookie;
    private String currentAuthCookie;
    private String currentPairingToken;
    private Account operatorAccount;

    @Before("@security")
    public void setUpScenario() {
        RestAssured.reset();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        databaseCleaner.clean();

        response = null;
        companyId = null;
        cashRegisterId = null;
        currentPosCookie = null;
        currentAuthCookie = null;
        currentPairingToken = null;
        operatorAccount = null;
    }

    @Dado("que o seed {string} carregou a tenant {string}")
    public void givenSeedLoadedTenant(String seedFile, String tenantId) {
        assertThat(seedFile).isEqualTo("R__seed_data.sql");
        tenantRepository.save(new Tenant(UUID.fromString(tenantId), "Conta de Teste Kalles"));
    }

    @Dado("existe a empresa {string}")
    public void givenCompanyExists(String companyName) {
        Company company = companyRepository.save(new Company(
                null,
                companyName,
                TENANT_ID,
                null,
                null,
                null,
                null,
                null,
                null
        ));
        companyId = company.getId();
    }

    @Dado("existe o caixa {string} para a empresa {string}")
    public void givenCashRegisterExists(String cashRegisterCode, String companyName) {
        assertThat(companyName).isEqualTo("Loja Matriz");
        cashRegisterId = cashRegisterRepository.save(
                new CashRegister(cashRegisterCode, "Caixa principal", companyId)
        ).getId();
    }

    @Dado("existe a conta web administradora {string} com senha {string}")
    public void givenAdminAccountExists(String email, String password) {
        accountRepository.save(newAccount("Administrador", email, password, AccountRole.ADMIN));
    }

    @Dado("existe a conta web operadora {string} com senha {string} vinculada a empresa {string}")
    public void givenOperatorAccountExists(String email, String password, String companyName) {
        assertThat(companyName).isEqualTo("Loja Matriz");
        operatorAccount = accountRepository.save(newAccount("Operador Caixa 01", email, password, AccountRole.OPERATOR));

        Operator operator = new Operator("Operador Caixa 01", "OP-CAIXA-01");
        operator.setCompanyId(companyId);
        operator.setPermissionLevel(PermissionLevel.BASIC);
        operatorRepository.save(operator);
    }

    @Dado("que o dispositivo nao possui o cookie {string}")
    public void givenDeviceHasNoCookie(String cookieName) {
        assertThat(cookieName).isEqualTo("kalles_pos_token");
        currentPosCookie = null;
    }

    @Quando("eu enviar o login com email {string} e senha {string}")
    public void whenISendLogin(String email, String password) {
        var request = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", password));

        if (currentPosCookie != null) {
            request.cookie("kalles_pos_token", currentPosCookie);
        }

        response = request.when().post("/api/auth/login");
        currentAuthCookie = response.getCookie("kalles_auth_token");
    }

    @Entao("a resposta de login deve ter status HTTP {int}")
    public void thenLoginResponseHasStatus(int statusCode) {
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    @Entao("a resposta deve definir o cookie de autenticacao {string}")
    public void thenResponseDefinesAuthCookie(String cookieName) {
        assertThat(cookieName).isEqualTo("kalles_auth_token");
        assertThat(currentAuthCookie).isNotBlank();
        assertThat(response.getHeaders().getValues(HttpHeaders.SET_COOKIE))
                .anyMatch(header -> header.contains("kalles_auth_token="));
    }

    @Entao("o login nao deve exigir pareamento de terminal para o perfil administrador")
    public void thenAdminLoginShouldNotRequirePairing() {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(currentAuthCookie).isNotBlank();
    }

    @Entao("a resposta deve conter a mensagem {string}")
    public void thenResponseContainsMessage(String message) {
        String body = response.asString();
        String detail = body;
        if (body != null && body.trim().startsWith("{")) {
            String jsonDetail = response.jsonPath().getString("detail");
            if (jsonDetail != null) {
                detail = jsonDetail;
            }
        }
        assertThat(normalize(detail)).contains(normalize(message));
    }

    @Dado("que estou autenticado como administrador da empresa {string}")
    public void givenAuthenticatedAsAdmin(String companyName) {
        assertThat(companyName).isEqualTo("Loja Matriz");
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "admin@sistema.local", "password", "123456"))
                .when()
                .post("/api/auth/login");
        currentAuthCookie = response.getCookie("kalles_auth_token");
        assertThat(currentAuthCookie).isNotBlank();
    }

    @Quando("eu solicitar a geracao de token de pareamento para o caixa {string}")
    public void whenIGeneratePairingTokenForCashRegister(String cashRegisterCode) {
        assertThat(cashRegisterCode).isEqualTo("CAIXA-01");
        CsrfContext csrf = CsrfTestClient.fetch(port);
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("kalles_auth_token", currentAuthCookie)
                .cookie("XSRF-TOKEN", csrf.csrfCookie())
                .header("X-XSRF-TOKEN", csrf.csrfToken())
                .body(Map.of(
                        "companyId", companyId,
                        "posId", cashRegisterId
                ))
                .when()
                .post("/api/pos/admin/generate-token");

        currentPairingToken = response.jsonPath().getString("pairingToken");
    }

    @Entao("a resposta deve ter status HTTP {int}")
    public void thenResponseHasStatus(int statusCode) {
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    @Entao("a resposta deve conter um {string} preenchido")
    public void thenResponseContainsFilledField(String fieldName) {
        assertThat(response.jsonPath().getString(fieldName)).isNotBlank();
    }

    @Entao("deve existir um pareamento de dispositivo pendente para o caixa {string}")
    public void thenPendingPairingShouldExist(String cashRegisterCode) {
        assertThat(cashRegisterCode).isEqualTo("CAIXA-01");
        assertThat(currentPairingToken).isNotBlank();
        assertThat(posDeviceSessionRepository.findByTokenAndActiveTrueAndExpiresAtGreaterThan(
                currentPairingToken,
                LocalDateTime.now().minusSeconds(1)
        )).isPresent();
    }

    @Dado("que existe um token de pareamento ativo para o caixa {string}")
    public void givenActivePairingTokenExists(String cashRegisterCode) {
        assertThat(cashRegisterCode).isEqualTo("CAIXA-01");
        currentPairingToken = seedPosDeviceSession("pairing-token-ativo", companyId, cashRegisterId, true);
    }

    @Quando("o dispositivo enviar o token de pareamento valido")
    public void whenDeviceSendsValidPairingToken() {
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("pairingToken", currentPairingToken))
                .when()
                .post("/api/pos/setup");
        currentPosCookie = response.getCookie("kalles_pos_token");
    }

    @Entao("a resposta de pareamento deve ter status HTTP {int}")
    public void thenPairingResponseHasStatus(int statusCode) {
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    @Entao("a resposta deve definir o cookie {string}")
    public void thenResponseDefinesCookie(String cookieName) {
        assertThat(cookieName).isEqualTo("kalles_pos_token");
        assertThat(currentPosCookie).isNotBlank();
        String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
        if (setCookie != null) {
            assertThat(setCookie).contains("kalles_pos_token=");
        }
    }

    @Entao("o dispositivo fica autorizado a operar o caixa {string} antes da abertura de sessao")
    public void thenDeviceCanOperateCashRegister(String cashRegisterCode) {
        assertThat(cashRegisterCode).isEqualTo("CAIXA-01");
        assertThat(currentPosCookie).isEqualTo(currentPairingToken);
    }

    @Dado("que o dispositivo possui um cookie {string} valido para o caixa {string} da empresa {string}")
    public void givenDeviceHasValidPosCookieForCompany(String cookieName, String cashRegisterCode, String companyName) {
        assertThat(cookieName).isEqualTo("kalles_pos_token");
        assertThat(cashRegisterCode).isEqualTo("CAIXA-01");
        assertThat(companyName).isEqualTo("Loja Matriz");
        currentPosCookie = seedPosDeviceSession("pairing-token-valido", companyId, cashRegisterId, true);
    }

    @Entao("o token autenticado deve carregar o identificador do caixa pareado")
    public void thenAuthenticatedTokenShouldCarryPairedPosId() {
        DecodedJWT decodedJWT = jwtService.validateToken(currentAuthCookie);
        assertThat(decodedJWT).isNotNull();
        assertThat(decodedJWT.getClaim("posId").asString()).isEqualTo(cashRegisterId.toString());
    }

    @Dado("que o dispositivo possui um cookie {string} revogado para o caixa {string}")
    public void givenDeviceHasRevokedCookie(String cookieName, String cashRegisterCode) {
        assertThat(cookieName).isEqualTo("kalles_pos_token");
        assertThat(cashRegisterCode).isEqualTo("CAIXA-01");
        currentPosCookie = seedPosDeviceSession("pairing-token-revogado", companyId, cashRegisterId, false);
    }

    @Dado("que o dispositivo possui um cookie {string} valido para um caixa de outra empresa")
    public void givenDeviceHasCookieFromAnotherCompany(String cookieName) {
        assertThat(cookieName).isEqualTo("kalles_pos_token");
        currentPosCookie = seedPosDeviceSession("pairing-token-outra-empresa", UUID.randomUUID(), UUID.randomUUID(), true);
    }

    @Quando("o dispositivo enviar um token de pareamento invalido")
    public void whenDeviceSendsInvalidPairingToken() {
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("pairingToken", "token-invalido"))
                .when()
                .post("/api/pos/setup");
    }

    @Quando("eu solicitar a geracao de token de pareamento sem informar companyId ou posId")
    public void whenIGeneratePairingTokenWithoutRequiredFields() {
        CsrfContext csrf = CsrfTestClient.fetch(port);
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("kalles_auth_token", currentAuthCookie)
                .cookie("XSRF-TOKEN", csrf.csrfCookie())
                .header("X-XSRF-TOKEN", csrf.csrfToken())
                .body(Map.of())
                .when()
                .post("/api/pos/admin/generate-token");
    }

    @Dado("que o operador esta autenticado")
    public void givenOperatorIsAuthenticated() {
        currentAuthCookie = jwtService.generateToken(operatorAccount);
        assertThat(currentAuthCookie).isNotBlank();
    }

    @Quando("ele tentar abrir sessao no caixa {string}")
    public void whenOperatorTriesToOpenSession(String cashRegisterCode) {
        CsrfContext csrf = CsrfTestClient.fetch(port);
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("kalles_auth_token", currentAuthCookie)
                .cookie("XSRF-TOKEN", csrf.csrfCookie())
                .header("X-XSRF-TOKEN", csrf.csrfToken())
                .body(Map.of(
                        "cashRegisterCode", cashRegisterCode,
                        "operatorCode", "OP-CAIXA-01",
                        "initialAmount", new BigDecimal("100.00")
                ))
                .when()
                .post("/api/cash-register-sessions/open");
    }

    @Entao("a resposta da abertura deve ter status HTTP {int}")
    public void thenOpenSessionResponseHasStatus(int statusCode) {
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    @Entao("a resposta deve indicar que o dispositivo precisa estar pareado antes da operacao")
    public void thenResponseShouldIndicatePairingIsRequired() {
        assertThat(normalize(response.jsonPath().getString("detail")))
                .contains("pareado antes da");
    }

    private Account newAccount(String name, String email, String rawPassword, AccountRole role) {
        Account account = new Account(TENANT_ID, name, email, passwordEncoder.encode(rawPassword), role);
        account.setCompanyId(companyId);
        account.setVerified(true);
        return account;
    }

    private String seedPosDeviceSession(String token, UUID targetCompanyId, UUID posId, boolean active) {
        PosDeviceSession session = new PosDeviceSession();
        session.setCompanyId(targetCompanyId);
        session.setPosId(posId);
        session.setToken(token);
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusDays(30));
        session.setActive(active);
        posDeviceSessionRepository.save(session);
        return token;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase();
    }
}
